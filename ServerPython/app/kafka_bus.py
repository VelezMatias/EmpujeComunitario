import json, os
from kafka import KafkaProducer


_producer = None

def get_producer():
    global _producer
    if _producer is None:
        _producer = KafkaProducer(
            bootstrap_servers=os.getenv("KAFKA_BOOTSTRAP", "localhost:9092"),
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
            key_serializer=lambda k: (k or "").encode("utf-8"),
            acks="all", linger_ms=10, retries=5
        )
    return _producer

def publish(topic: str, key: str | None, payload: dict):
    p = get_producer()
    p.send(topic, key=key, value=payload)
    p.flush()



def publish_baja_evento(org_id: int | str, evento_id: int | str) -> None:
    """
    Se publica la baja de un evento propio en el topic 'baja-evento-solidario'
    usando la función publish(topic, key, payload).

    - key: str(org_id)
    - payload: {
        "org_id": <int>,
        "evento_id": "<str>",
        "idempotency_key": "BAJA_EVENTO:<org_id>:<evento_id>"
      }
    """
    # normalización de tipos para garantizar coherencia con el worker
    org_id_int = int(org_id)
    org_id_str = str(org_id_int)
    evento_id_str = str(evento_id)

    payload = {
        "org_id": org_id_int,  # entero en el JSON (el worker lo castea a int)
        "evento_id": evento_id_str,
        "idempotency_key": f"BAJA_EVENTO:{org_id_str}:{evento_id_str}",
    }

    publish("baja-evento-solidario", key=org_id_str, payload=payload)
