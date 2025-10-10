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