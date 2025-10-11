import grpc
from concurrent import futures

from app.db import Base, engine
from app import models
from app.config import GRPC_HOST, GRPC_PORT

from app import ong_pb2_grpc as rpc

from app.services.user_service import UserServiceServicer
from app.services.donation_service import DonationServiceServicer
from app.services.event_service import EventServiceServicer

def init_db():
    Base.metadata.create_all(bind=engine)

def serve():
    init_db()
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    rpc.add_UserServiceServicer_to_server(UserServiceServicer(), server)
    rpc.add_DonationServiceServicer_to_server(DonationServiceServicer(), server)
    rpc.add_EventServiceServicer_to_server(EventServiceServicer(), server)

    bind = f"{GRPC_HOST}:{GRPC_PORT}"
    server.add_insecure_port(bind)
    print(f"[gRPC] Servidor escuchando en {bind}")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()