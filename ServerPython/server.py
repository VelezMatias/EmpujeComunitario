import os
import grpc
from concurrent import futures
from dotenv import load_dotenv
load_dotenv()

import ong_pb2_grpc
from app.services.user_service import UserServiceServicer
from app.db import get_conn  # para testear conexión

def serve():
    # Test rápido de conexión a MySQL
    try:
        conn = get_conn()
        conn.close()
    except Exception as e:
        print(f"[DB][ERROR] No se pudo conectar a MySQL: {e}")

    port = os.getenv("GRPC_PORT", "50051")
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))

    ong_pb2_grpc.add_UserServiceServicer_to_server(UserServiceServicer(), server)

    server.add_insecure_port(f"[::]:{port}")
    print(f"[gRPC][Python] Escuchando en 0.0.0.0:{port} ...")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()
