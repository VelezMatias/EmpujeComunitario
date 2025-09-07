import os
from urllib.parse import urlparse
import pymysql
from pymysql.cursors import DictCursor

# Puedes definir conexión por variables de entorno o por URL:
# 1) Por variables:
#   DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME
# 2) O por URL:
#   DB_URL = "mysql+pymysql://user:pass@host:3306/empuje_comunitario"

def _from_env():
    return dict(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "3306")),
        user=os.getenv("DB_USER", "root"),
        password=os.getenv("DB_PASS", ""),
        database=os.getenv("DB_NAME", "empuje_comunitario"),
    )

def _from_url(url: str):
    # Soporta formato mysql+pymysql://user:pass@host:port/db
    p = urlparse(url.replace("mysql+pymysql://", "mysql://", 1))
    return dict(
        host=p.hostname or "localhost",
        port=p.port or 3306,
        user=p.username or "root",
        password=p.password or "",
        database=(p.path or "/empuje_comunitario").lstrip("/"),
    )

def get_conn():
    if os.getenv("DB_URL"):
        cfg = _from_url(os.getenv("DB_URL"))
    else:
        cfg = _from_env()
    conn = pymysql.connect(
        host=cfg["host"],
        port=cfg["port"],
        user=cfg["user"],
        password=cfg["password"],
        database=cfg["database"],
        autocommit=False,
        cursorclass=DictCursor,
        charset="utf8mb4",
    )
    print(f'[DB] Conectado a MySQL {cfg["host"]}:{cfg["port"]}/{cfg["database"]}')
    return conn

def execute(query: str, params: tuple = ()):
    """INSERT/UPDATE/DELETE. Devuelve rowcount e id insertado (si aplica)."""
    conn = get_conn()
    try:
        with conn.cursor() as cur:
            cur.execute(query, params)
            last_id = cur.lastrowid
        conn.commit()
        return cur.rowcount, last_id
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()

def fetch_one(query: str, params: tuple = ()):
    conn = get_conn()
    try:
        with conn.cursor() as cur:
            cur.execute(query, params)
            return cur.fetchone()
    finally:
        conn.close()

def fetch_all(query: str, params: tuple = ()):
    conn = get_conn()
    try:
        with conn.cursor() as cur:
            cur.execute(query, params)
            return cur.fetchall()
    finally:
        conn.close()
