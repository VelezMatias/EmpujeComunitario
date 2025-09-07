# db.py
# Módulo simple para obtener conexiones a MySQL usando variables de entorno.
# Si alguna variable no está definida, usa valores por defecto coherentes
# con tu dump (BD: empujecomunitario).

import os
import mysql.connector
from mysql.connector import pooling

# Lee variables de entorno (puedes crear un .env)
MYSQL_HOST = os.getenv("MYSQL_HOST", "localhost")
MYSQL_PORT = int(os.getenv("MYSQL_PORT", "3306"))
MYSQL_DB   = os.getenv("MYSQL_DB", "empujecomunitario")
MYSQL_USER = os.getenv("MYSQL_USER", "root")   # poner usuario de la DB
MYSQL_PASS = os.getenv("MYSQL_PASSWORD", "12345")   # Poner contraseña de la BD

cnx_pool = pooling.MySQLConnectionPool(
    pool_name="ec_pool",
    pool_size=5,
    host=MYSQL_HOST,
    port=MYSQL_PORT,
    database=MYSQL_DB,
    user=MYSQL_USER,
    password=MYSQL_PASS,
    auth_plugin="mysql_native_password"  
)

def get_connection():
    """
    Devuelve una conexión lista para usar.
    Debes cerrar la conexión luego de usarla (conn.close()).
    """
    return cnx_pool.get_connection()
