# server.py
import os
import signal
import sys
import grpc
from concurrent import futures
from dotenv import load_dotenv

# Cargar variables de entorno (.env)
load_dotenv()

# Stubs gRPC generados para *ambos* servicios (mismo paquete "app")
from app import ong_pb2_grpc
from app.services.user_service import UserServiceServicer
from app.services.event_service import EventServiceServicer
from app.services.donation_service import DonationServiceServicer
from app.db import get_conn  # para testear conexión
from app.services.externals_service import ExternalsService

# Conexión a MySQL (para testeo rápido al inicio)
from app.db import get_conn


def serve():
    # 1) Test rápido de conexión a MySQL
    try:
        conn = get_conn()
        conn.close()
        print("[DB] Conectado a MySQL OK")
    except Exception as e:
        print(f"[DB][ERROR] No se pudo conectar a MySQL: {e}")

    # 2) Configuración del servidor gRPC
    port = os.getenv("GRPC_PORT", "50051")
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))

    # 3) Registrar servicios
    #    - Servicio de Usuarios (ya existente)
    ong_pb2_grpc.add_UserServiceServicer_to_server(UserServiceServicer(), server)
    ong_pb2_grpc.add_EventServiceServicer_to_server(EventServiceServicer(), server)
    ong_pb2_grpc.add_DonationServiceServicer_to_server(DonationServiceServicer(), server)


    #    - Servicio de Externos (nuevos RPCs para UI: ListSolicitudesExternas / ListEventosExternos)
    ong_pb2_grpc.add_OngServiceServicer_to_server(ExternalsService(), server)

    # 4) Bind y start
    server.add_insecure_port(f"[::]:{port}")
    print(f"[gRPC][Python] Escuchando en 0.0.0.0:{port} ...")
    server.start()

    # 5) Apagado elegante ante Ctrl+C / SIGTERM
    def handle_signal(signum, frame):
        print(f"\n[GRPC] Señal {signum} recibida. Cerrando servidor...")
        server.stop(grace=None)  # espera a terminar RPCs activos
        sys.exit(0)

    signal.signal(signal.SIGINT, handle_signal)
    signal.signal(signal.SIGTERM, handle_signal)

    server.wait_for_termination()


if __name__ == "__main__":
    serve()
