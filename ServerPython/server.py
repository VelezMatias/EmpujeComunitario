import os
import grpc
from concurrent import futures

import users_pb2_grpc
from services.users_service import UsersService

def serve():
    port = os.getenv("GRPC_PORT", "50051")
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))

    # ¡OJO al nombre!: add_UsersServiceServicer_to_server
    users_pb2_grpc.add_UsersServiceServicer_to_server(UsersService(), server)

    server.add_insecure_port(f"[::]:{port}")
    print(f"[gRPC][Python] Escuchando en 0.0.0.0:{port} ...")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()
