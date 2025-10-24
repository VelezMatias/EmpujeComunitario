import json, os, time, sys
from datetime import datetime
import pymysql
from confluent_kafka import Consumer, KafkaException, KafkaError
from dotenv import load_dotenv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

load_dotenv()

from app.emailer import send_adhesion_notification

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "3306"))
DB_NAME = os.getenv("DB_NAME", "empujecomunitario")
DB_USER = os.getenv("DB_USER", "empuje")
DB_PASS = os.getenv("DB_PASS", "empuje123")

KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "127.0.0.1:9092")
ORG_ID = os.getenv("ORG_ID", "42")  # para topics con sufijo

TOPICS = [
    "solicitud-donaciones",
    "baja-solicitud-donaciones",
    "eventos-solidarios",
    "baja-evento-solidario",
    f"transferencia-donaciones.{ORG_ID}",
    f"adhesion-evento.{ORG_ID}",
]

def get_db():
    return pymysql.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS,
        database=DB_NAME, autocommit=False, charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor
    )

def iso_to_dt(s):
    try:
        return s[:19].replace("T", " ")
    except Exception:
        return datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")

def already_processed(cur, topic, key):
    cur.execute("SELECT 1 FROM mensajes_procesados WHERE topic=%s AND message_key=%s LIMIT 1", (topic, key))
    return cur.fetchone() is not None

def mark_processed(cur, topic, key, part, off):
    cur.execute("""
        INSERT INTO mensajes_procesados (topic, message_key, partition_no, offset_no)
        VALUES (%s,%s,%s,%s)
    """, (topic, key, part, off))

# ---------- Handlers ----------
def handle_solicitud(cur, payload):
    org_id = int(payload["org_id"])
    solicitud_id = str(payload["solicitud_id"])
    fecha_dt = iso_to_dt(payload.get("fecha_hora",""))
    estado = payload.get("estado","VIGENTE")
    cur.execute("""
        INSERT INTO solicitudes_externas (org_id, solicitud_id, fecha_hora, estado, payload_json)
        VALUES (%s,%s,%s,%s,%s)
        ON DUPLICATE KEY UPDATE fecha_hora=VALUES(fecha_hora), estado=VALUES(estado), payload_json=VALUES(payload_json)
    """, (org_id, solicitud_id, fecha_dt, estado, json.dumps(payload, ensure_ascii=False)))

def handle_baja_solicitud(cur, payload):
    org_id = int(payload["org_id"])
    solicitud_id = str(payload["solicitud_id"])
    cur.execute("""
        UPDATE solicitudes_externas SET estado='BAJA'
        WHERE org_id=%s AND solicitud_id=%s
    """, (org_id, solicitud_id))

def handle_evento(cur, payload):
    org_id = int(payload["org_id"])
    evento_id = str(payload["evento_id"])
    # Los docs usan fecha_inicio/fecha_fin; aceptar fecha_hora o fecha_inicio
    fecha_dt = iso_to_dt(
        payload.get("fecha_hora") or payload.get("fecha_inicio") or ""
    )
    estado = payload.get("estado","VIGENTE")
    # Descartar eventos propios (solo guardar de otras organizaciones)
    try:
        my_org = int(ORG_ID)
        if org_id == my_org:
            print(f"[SKIP] evento propio org_id={org_id} evento_id={evento_id}")
            return  # ignorar
    except Exception:
        pass
    cur.execute("""
        INSERT INTO eventos_externos (org_id, evento_id, fecha_hora, estado, payload_json)
        VALUES (%s,%s,%s,%s,%s)
        ON DUPLICATE KEY UPDATE fecha_hora=VALUES(fecha_hora), estado=VALUES(estado), payload_json=VALUES(payload_json)
    """, (org_id, evento_id, fecha_dt, estado, json.dumps(payload, ensure_ascii=False)))

def handle_baja_evento(cur, payload):
    org_id = int(payload["org_id"])
    evento_id = str(payload["evento_id"])

     # Ignora bajas propias (no toca "eventos_externos" para nuestra propia org)
    try:
        if org_id == int(ORG_ID):
            return
    except Exception:
        pass

    cur.execute("""
        UPDATE eventos_externos SET estado='BAJA'
        WHERE org_id=%s AND evento_id=%s
    """, (org_id, evento_id))

# stubs para más adelante
def handle_transferencia(cur, payload):
    org_id_origen = int(payload.get("org_id_origen") or 0)
    org_id_destino = int(payload.get("org_id_destino") or 0)
    solicitud_id = str(payload.get("solicitud_id") or "")
    fecha_dt = iso_to_dt(payload.get("fecha_hora", ""))
    idem_key = str(payload.get("idempotency_key") or f"TRF:{org_id_origen}->{org_id_destino}:{solicitud_id}")

    cur.execute(
        """
        INSERT INTO transferencias_externas (
            org_id_origen, org_id_destino, solicitud_id, fecha_hora, idempotency_key, payload_json
        ) VALUES (%s,%s,%s,%s,%s,%s)
        ON DUPLICATE KEY UPDATE
            fecha_hora = VALUES(fecha_hora),
            payload_json = VALUES(payload_json)
        """,
        (
            org_id_origen,
            org_id_destino,
            solicitud_id,
            fecha_dt,
            idem_key,
            json.dumps(payload, ensure_ascii=False),
        ),
    )


def handle_adhesion(cur, payload):
    org_id_org = int(payload.get("org_id_organizador") or 0)
    org_id_adh = int(payload.get("org_id_adherente") or 0)
    evento_id = str(payload.get("evento_id") or "")
    fecha_dt = iso_to_dt(payload.get("fecha_hora", ""))
    idem_key = str(payload.get("idempotency_key") or f"ADH:{org_id_adh}->{org_id_org}:{evento_id}")

    cur.execute(
        """
        INSERT INTO adhesiones_evento (
            org_id_organizador, evento_id, org_id_adherente, fecha_hora, idempotency_key, payload_json
        ) VALUES (%s,%s,%s,%s,%s,%s)
        ON DUPLICATE KEY UPDATE
            fecha_hora = VALUES(fecha_hora),
            payload_json = VALUES(payload_json)
        """,
        (
            org_id_org,
            evento_id,
            org_id_adh,
            fecha_dt,
            idem_key,
            json.dumps(payload, ensure_ascii=False),
        ),
    )

    # Intentar notificar por email al organizador; si no hay email, pasamos None y el emailer hará el fallback
    try:
        to_email = None
        try:
            cur.execute("SELECT email FROM usuarios WHERE id = %s LIMIT 1", (org_id_org,))
            row = cur.fetchone()
            to_email = row.get("email") if row else None
        except Exception as e:
            print(f"[NOTIFY] Error buscando email de organizador id={org_id_org}: {e}")

        # Logs útiles para depuración
        print(f"[NOTIFY] Payload recibido: {json.dumps(payload, ensure_ascii=False)}")
        print(f"[NOTIFY] Email organizador encontrado: {to_email}")

        try:
            send_adhesion_notification(to_email, evento_id, org_id_adh, fecha_dt)
            print(f"[NOTIFY] Intentada notificación de adhesión para evento {evento_id} (destino: {to_email or 'EMAIL_USER (fallback)'})")
        except Exception as e:
            print(f"[NOTIFY ERROR] Fallo al enviar notificación a {to_email or 'EMAIL_USER'}: {e}")
    except Exception:
        pass

HANDLERS = {
    "solicitud-donaciones": handle_solicitud,
    "baja-solicitud-donaciones": handle_baja_solicitud,
    "eventos-solidarios": handle_evento,
    "baja-evento-solidario": handle_baja_evento,
    # dinámicos:
    "transferencia-donaciones": handle_transferencia,
    "adhesion-evento": handle_adhesion,
}

def route(cur, topic, payload):
    # soporta topics dinámicos: "transferencia-donaciones.42"
    base = topic.split(".")[0]
    h = HANDLERS.get(topic) or HANDLERS.get(base)
    if not h:
        raise RuntimeError(f"Topic sin handler: {topic}")
    h(cur, payload)

def main():
    conf = {
        "bootstrap.servers": KAFKA_BOOTSTRAP,
        "group.id": "ec-worker",
        "auto.offset.reset": "earliest",
        "enable.auto.commit": False,
    }
    c = Consumer(conf)
    c.subscribe(TOPICS)
    print(f"[Kafka][Worker] Suscripto a: {', '.join(TOPICS)} @ {KAFKA_BOOTSTRAP}")

    conn = get_db()
    try:
        while True:
            msg = c.poll(1.0)
            if msg is None: continue
            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF: continue
                raise KafkaException(msg.error())

            topic = msg.topic(); part = msg.partition(); off = msg.offset()
            key = msg.key().decode("utf-8") if msg.key() else ""
            try:
                payload = json.loads(msg.value().decode("utf-8"))
            except Exception:
                print(f"[WARN] Mensaje no JSON en {topic}@{part}:{off}")
                c.commit(message=msg); continue

            message_key = payload.get("idempotency_key") or key or ""
            if not message_key:
                # fallbacks típicos
                message_key = payload.get("solicitud_id") or payload.get("evento_id") or f"{topic}:{off}"

            try:
                with conn.cursor() as cur:
                    if already_processed(cur, topic, message_key):
                        conn.commit(); c.commit(message=msg)
                        print(f"[SKIP] {topic} key={message_key} off={off}")
                        continue

                    route(cur, topic, payload)
                    mark_processed(cur, topic, message_key, part, off)
                    conn.commit(); c.commit(message=msg)
                    print(f"[OK] {topic} key={message_key} off={off}")
            except Exception as e:
                conn.rollback()
                print(f"[ERROR] {topic}@{part}:{off} -> {e}")
                time.sleep(1)
    except KeyboardInterrupt:
        print("Saliendo…")
    finally:
        try: conn.close()
        except: pass
        c.close()

if __name__ == "__main__":
    main()
