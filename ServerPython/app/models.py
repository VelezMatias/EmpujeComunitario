"""
DAO sin ORM para esquema MySQL con tablas:
  - usuarios, roles, eventos, donaciones, evento_participantes, categorias
De momento implementamos usuarios/roles (login/registro).
"""

from typing import Optional, List, Dict
from app.db import fetch_one, fetch_all, execute

# ========== USUARIOS ==========

def usuario_por_username(username: str) -> Optional[Dict]:
    sql = "SELECT * FROM usuarios WHERE username = %s LIMIT 1"
    return fetch_one(sql, (username,))

def usuario_por_email(email: str) -> Optional[Dict]:
    sql = "SELECT * FROM usuarios WHERE email = %s LIMIT 1"
    return fetch_one(sql, (email,))

def usuario_por_identifier(identifier: str) -> Optional[Dict]:
    # username o email
    sql = """
      SELECT * FROM usuarios
      WHERE username = %s OR email = %s
      LIMIT 1
    """
    return fetch_one(sql, (identifier, identifier))

def crear_usuario(*, username: str, nombre: str, apellido: str, telefono: str,
                  email: str, password_hash: str, rol_id: int, activo: int = 1) -> int:
    sql = """
      INSERT INTO usuarios (username, nombre, apellido, telefono, password, email, activo, rol_id)
      VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """
    _, last_id = execute(sql, (username, nombre, apellido, telefono, password_hash, email, activo, rol_id))
    return last_id

def actualizar_usuario(*, id_usuario: int, nombre: Optional[str], apellido: Optional[str],
                       telefono: Optional[str], email: Optional[str],
                       rol_id: Optional[int], activo: Optional[int]) -> int:
    campos = []
    params = []
    if nombre is not None:
        campos.append("nombre=%s"); params.append(nombre)
    if apellido is not None:
        campos.append("apellido=%s"); params.append(apellido)
    if telefono is not None:
        campos.append("telefono=%s"); params.append(telefono)
    if email is not None:
        campos.append("email=%s"); params.append(email)
    if rol_id is not None:
        campos.append("rol_id=%s"); params.append(rol_id)
    if activo is not None:
        campos.append("activo=%s"); params.append(activo)
    if not campos:
        return 0
    params.append(id_usuario)
    sql = f"UPDATE usuarios SET {', '.join(campos)} WHERE id = %s"
    rowcount, _ = execute(sql, tuple(params))
    return rowcount

def desactivar_usuario(id_usuario: int) -> int:
    sql = "UPDATE usuarios SET activo = 0 WHERE id = %s"
    rowcount, _ = execute(sql, (id_usuario,))
    return rowcount

def listar_usuarios() -> List[Dict]:
    # Si tu tabla roles usa 'id' como PK (recomendado):
    sql = """
      SELECT u.id, u.username, u.nombre, u.apellido, u.telefono,
             u.email, u.rol_id, u.activo, r.nombre AS rol_nombre
      FROM usuarios u
      LEFT JOIN roles r ON r.id = u.rol_id
      ORDER BY u.id ASC
    """
    # Si en tu BD 'roles' usa 'id_rol' como PK, cambia ON r.id = u.rol_id -> ON r.id_rol = u.rol_id
    return fetch_all(sql)

# ========== ROLES ==========

def rol_existe(rol_id: int) -> bool:
    # Si roles.id es la PK:
    sql = "SELECT 1 AS ok FROM roles WHERE id = %s"
    # Si fuera id_rol, cambia a: "SELECT 1 AS ok FROM roles WHERE id_rol = %s"
    return fetch_one(sql, (rol_id,)) is not None
