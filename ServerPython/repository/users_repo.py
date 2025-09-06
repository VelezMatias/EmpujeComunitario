# repository/users_repo.py
# Consultas mínimas sobre la tabla usuarios.

from typing import Optional, Dict
from db import get_connection

class UsersRepo:

    @staticmethod
    def get_by_id(user_id: int) -> Optional[Dict]:
        sql = """
            SELECT id, username, nombre, apellido, email, telefono, rol_id, activo
            FROM usuarios WHERE id = %s
        """
        with get_connection() as conn, conn.cursor() as cur:
            cur.execute(sql, (user_id,))
            return cur.fetchone()

    @staticmethod
    def get_by_username(username: str) -> Optional[Dict]:
        sql = """
            SELECT id, username, nombre, apellido, email, telefono, rol_id, activo, password
            FROM usuarios WHERE username = %s
        """
        with get_connection() as conn, conn.cursor() as cur:
            cur.execute(sql, (username,))
            return cur.fetchone()

    @staticmethod
    def create(username: str, nombre: str, apellido: str, email: str, telefono: str, password: str, rol_id: int = 4) -> int:
        # rol_id=4 VOLUNTARIO por defecto
        sql = """
            INSERT INTO usuarios (username, nombre, apellido, telefono, password, email, rol_id, activo)
            VALUES (%s, %s, %s, %s, %s, %s, %s, 1)
        """
        with get_connection() as conn, conn.cursor() as cur:
            cur.execute(sql, (username, nombre, apellido, telefono, password, email, rol_id))
            return cur.lastrowid
