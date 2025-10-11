Empuje Comunitario – Item 5 (Kafka externos)

Este repo ya tiene implementados los puntos 1, 3 y 4. En este commit se agrega el punto 5:

- Consumidor Python que procesa mensajes externos adicionales:
	- transferencia-donaciones.<org_id_destino>
	- adhesion-evento.<org_id_organizador>
- Persistencia idempotente en MySQL de ambos flujos.
- DDL agregado en BD KAFKA_item5.sql para crear las tablas necesarias.

Cómo correr (Windows PowerShell):

1) Infra Kafka
- Ir a infra/kafka y levantar docker compose.

2) DB
- Ejecutar BD KAFKA.sql y luego BD KAFKA_item5.sql en MySQL.

3) Variables .env (ServerPython/.env)
- KAFKA_BOOTSTRAP_SERVERS=127.0.0.1:9092
- ORG_ID=42 (org destino para suscripción dinámica)
- DB_HOST, DB_USER, DB_PASS apuntando a tu MySQL

4) Consumidor
- Ejecutar ServerPython/consumers/worker.py. Verás suscripto a:
	solicitud-donaciones, baja-solicitud-donaciones, eventos-solidarios, baja-evento-solidario, transferencia-donaciones.42, adhesion-evento.42

5) Publicar mensajes de prueba
- Usar el Cliente Spring (endpoints /kafka/*) o enviar manualmente a Kafka.
- Esquemas JSON en docs/kafka-payloads.md.

Notas
- El consumidor usa idempotency_key para evitar reprocesos (tabla mensajes_procesados y claves únicas en nuevas tablas).
