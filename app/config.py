import os
from dotenv import load_dotenv

load_dotenv()

DB_URL = os.getenv("DB_URL", "sqlite:///./ong.db")  # para desarrollo
GRPC_HOST = os.getenv("GRPC_HOST", "[::]")
GRPC_PORT = int(os.getenv("GRPC_PORT", "50051"))