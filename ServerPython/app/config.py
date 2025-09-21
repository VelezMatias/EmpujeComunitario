import os
from dotenv import load_dotenv

load_dotenv()

DB_URL = "mysql+pymysql://empuje:empuje123@localhost:3306/empujecomunitario"
GRPC_HOST = "0.0.0.0"
GRPC_PORT = 50051

# Config de mail
EMAIL_HOST = os.getenv("EMAIL_HOST", "smtp.live.com")
EMAIL_PORT = int(os.getenv("EMAIL_PORT", "587"))
EMAIL_USER = os.getenv("EMAIL_USER", "")
EMAIL_PASSWORD = os.getenv("EMAIL_PASSWORD", "")
EMAIL_FROM_NAME = os.getenv("EMAIL_FROM_NAME", "Empuje Comunitario")
