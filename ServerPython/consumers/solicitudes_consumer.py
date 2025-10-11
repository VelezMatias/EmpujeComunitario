import json
import os
import sys
import time
from datetime import datetime

import pymysql
from confluent_kafka import Consumer, KafkaException, KafkaError
from dotenv import load_dotenv

# Cargar .env
load_dotenv()

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "3306"))
DB_NAME = os.getenv("DB_NAME", "empujecomunitario")
DB_USER = os.getenv("DB_USER", "empuje")
DB_PASS = os.getenv("DB_PASS", "empuje123")

KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

TOPIC = "solicitud-donaciones"  # arrancamos con este; luego sumamos otros

def get_db_conn():
    return pymysql.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS,
        database=DB_NAME, autocommit=False, charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor
    )

def persist_solicitud_externa(cur, payload: dict):
    """
    Inserta/actualiza la solicitud en solicitudes_externas.
    Espera payload con: org_id, solicitud_id, fecha_hora, (opcional) estado, y el JSON crudo.
    """
    org_id = int(payload["org_id"])
    solicitud_id = str(payload["solicitud_id"])

    # fecha_hora viene ISO; normalizamos a DATETIME sin zona (naive)
    # ejemplo: "2025-09-15T03:10:00-03:00" -> '2025-09-15 03:10:00'
    fecha_hora_str = payload.get("fecha_hora", "")
    try:
        fecha_dt = fecha_hora_str[:19].replace("T", " ")
    except Exception:
        fecha_dt = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")

    estado = payload.get("estado", "VIGENTE")

    cur.execute(
        """
        INSERT INTO solicitudes_externas (org_id, solicitud_id, fecha_hora, estado, payload_json)
        VALUES (%s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            fecha_hora = VALUES(fecha_hora),
            estado = VALUES(estado),
            payload_json = VALUES(payload_json)
        """,
        (org_id, solicitud_id, fecha_dt, estado, json.dumps(payload, ensure_ascii=False))
    )

def registrar_mensaje_procesado(cur, topic, message_key, partition, offset):
    cur.execute(
        """
        INSERT INTO mensajes_procesados (topic, message_key, partition_no, offset_no)
        VALUES (%s, %s, %s, %s)
        """,
        (topic, message_key, partition, offset)
    )

def ya_procesado(cur, topic, message_key):
    cur.execute(
        "SELECT 1 FROM mensajes_procesados WHERE topic=%s AND message_key=%s LIMIT 1",
        (topic, message_key)
    )
    return cur.fetchone() is not None

def build_message_key(payload: dict, key: str | None) -> str:
    """
    Calcula una clave de idempotencia estable por solicitud:
    1) payload.idempotency_key
    2) f"{org_id}:{solicitud_id}"
    3) key de Kafka (fallback)
    """
    # Normalizar campos
    sid = str(payload.get("solicitud_id", "")).strip()
    oid = str(payload.get("org_id", "")).strip()
    idk = payload.get("idempotency_key")

    if isinstance(idk, str):
        idk = idk.strip()
    else:
        idk = None

    if idk:  # (1) respetar idempotency_key si viene
        return idk

    if oid and sid:  # (2) org_id:solicitud_id
        return f"{oid}:{sid}"

    # (3) fallback: key del record
    return (key or "").strip()

def main():
    conf = {
        "bootstrap.servers": KAFKA_BOOTSTRAP,
        "group.id": "ec-solicitudes-consumer",  # nombre del consumer group
        "auto.offset.reset": "earliest",
        "enable.auto.commit": False,            # commit manual sólo si DB ok
        "max.poll.interval.ms": 300000,
        "session.timeout.ms": 10000,
    }

    consumer = Consumer(conf)
    consumer.subscribe([TOPIC])

    print(f"[Kafka][Consumer] Escuchando topic '{TOPIC}' en {KAFKA_BOOTSTRAP} ...")

    conn = get_db_conn()
    try:
        while True:
            msg = consumer.poll(1.0)
            if msg is None:
                continue
            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    continue
                raise KafkaException(msg.error())

            topic = msg.topic()
            partition = msg.partition()
            offset = msg.offset()
            key = msg.key().decode("utf-8") if msg.key() else None
            value = msg.value().decode("utf-8")

            # Parseo de JSON
            try:
                payload = json.loads(value)
            except json.JSONDecodeError:
                print(f"[WARN] Mensaje no-JSON en {topic}@{partition}:{offset} -> {value!r}")
                # descartamos (o podés mandarlo a una DLQ)
                consumer.commit(message=msg)
                continue

            # Idempotencia robusta
            message_key = build_message_key(payload, key)
            if not message_key:
                # último fallback defensivo
                mk_fallback = f"{payload.get('org_id','')}:{payload.get('solicitud_id','')}".strip()
                message_key = mk_fallback or (key or "")
            # log útil
            print(f"[DEBUG] Calc message_key='{message_key}' for key='{key}' payload.solicitud_id='{payload.get('solicitud_id')}'")

            try:
                with conn.cursor() as cur:
                    # idempotencia lógica
                    if ya_procesado(cur, topic, message_key):
                        print(f"[SKIP] Duplicado topic={topic} key={message_key} offset={offset}")
                        conn.commit()                 # no tocamos datos, pero avanzamos estado de transacción
                        consumer.commit(message=msg)  # avanzamos offset
                        continue

                    # persistimos la solicitud
                    persist_solicitud_externa(cur, payload)

                    # registramos que ya se procesó (mismo tx)
                    registrar_mensaje_procesado(cur, topic, message_key, partition, offset)

                    conn.commit()
                    consumer.commit(message=msg)
                    print(f"[OK] topic={topic} key={message_key} offset={offset}")

            except Exception as e:
                conn.rollback()
                print(f"[ERROR] DB/Proc: {e} -> no se commitea offset, se volverá a intentar")
                time.sleep(1)

    except KeyboardInterrupt:
        print("Cerrando consumer...")
    finally:
        try:
            conn.close()
        except Exception:
            pass
        consumer.close()

if __name__ == "__main__":
    main()