"""
DAO sin ORM para esquema MySQL con tablas:
  - usuarios, roles, eventos, donaciones, evento_participantes, categorias
De momento implementamos usuarios/roles (login/registro).
"""

from typing import Optional, List, Dict
from datetime import datetime
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


# ========== EVENTOS ==========



def crear_evento(*, nombre: str, descripcion: str, fecha_utc: datetime, creador_id: int) -> int:
    """
    Inserta un evento. fecha_utc debe venir como datetime UTC.
    Guarda en DATETIME (sin tz) asumiendo UTC.
    """
    sql = """
      INSERT INTO eventos (nombre, descripcion, fecha_hora, creador_id)
      VALUES (%s, %s, %s, %s)
    """
    fecha_str = fecha_utc.strftime('%Y-%m-%d %H:%M:%S')
    _, last_id = execute(sql, (nombre, descripcion, fecha_str, creador_id))
    return last_id

def evento_por_id(id_evento: int):
    sql = "SELECT id, nombre, descripcion, fecha_hora, creador_id FROM eventos WHERE id = %s LIMIT 1"
    row = fetch_one(sql, (id_evento,))
    if not row:
        return None
    # Normalizamos fecha_hora a datetime (puede venir ya como datetime)
    fh = row["fecha_hora"]
    if isinstance(fh, str):
        fh = datetime.strptime(fh, '%Y-%m-%d %H:%M:%S')
    row["fecha_hora"] = fh
    return row

def actualizar_evento(*, id_evento: int,
                      nombre: str | None,
                      descripcion: str | None,
                      fecha_utc: datetime | None) -> int:
    campos = []
    params = []
    if nombre is not None:
        campos.append("nombre=%s"); params.append(nombre)
    if descripcion is not None:
        campos.append("descripcion=%s"); params.append(descripcion)
    if fecha_utc is not None:
        campos.append("fecha_hora=%s"); params.append(fecha_utc.strftime('%Y-%m-%d %H:%M:%S'))
    if not campos:
        return 0
    params.append(id_evento)
    sql = f"UPDATE eventos SET {', '.join(campos)} WHERE id = %s"
    rowcount, _ = execute(sql, tuple(params))
    return rowcount

def eliminar_evento(id_evento: int) -> int:
    sql = "DELETE FROM eventos WHERE id = %s"
    rowcount, _ = execute(sql, (id_evento,))
    return rowcount

def listar_eventos():
    sql = """
      SELECT id, nombre, descripcion, fecha_hora, creador_id
      FROM eventos
      ORDER BY fecha_hora DESC, id DESC
    """
    rows = fetch_all(sql)
    return rows




def miembros_ids_por_evento(event_id: int):
    sql = "SELECT user_id FROM evento_participantes WHERE event_id = %s"
    rows = fetch_all(sql, (event_id,))
    return [r["user_id"] for r in rows] if rows else []

def evento_tiene_miembro(event_id: int, user_id: int) -> bool:
    sql = "SELECT 1 AS ok FROM evento_participantes WHERE event_id=%s AND user_id=%s LIMIT 1"
    return fetch_one(sql, (event_id, user_id)) is not None

def agregar_miembro_evento(event_id: int, user_id: int) -> int:
    sql = "INSERT INTO evento_participantes (event_id, user_id) VALUES (%s, %s)"
    rowcount, _ = execute(sql, (event_id, user_id))
    return rowcount

def quitar_miembro_evento(event_id: int, user_id: int) -> int:
    sql = "DELETE FROM evento_participantes WHERE event_id=%s AND user_id=%s"
    rowcount, _ = execute(sql, (event_id, user_id))
    return rowcount

def usuario_por_id(id_usuario: int):
    sql = "SELECT id, activo FROM usuarios WHERE id = %s LIMIT 1"
<<<<<<< Updated upstream
    return fetch_one(sql, (id_usuario,))
=======
    return fetch_one(sql, (id_usuario,))



#------------- Evento - Donacion ------------------------------
def assign_donation_to_event(conn, event_id: int, donation_id: int, cantidad: int):
    if cantidad is None or cantidad < 0:
        return False, "Cantidad inválida"
    with conn.cursor() as cur:
        # (opcional) validar existencia
        cur.execute("SELECT 1 FROM eventos WHERE id=%s", (event_id,))
        if not cur.fetchone():
            return False, "Evento no existe"
        cur.execute("SELECT 1 FROM donaciones WHERE id=%s AND eliminado=0", (donation_id,))
        if not cur.fetchone():
            return False, "Donación no existe o está eliminada"

        # upsert por UNIQUE(evento_id, donacion_id)
        cur.execute("""
            INSERT INTO evento_donacion (evento_id, donacion_id, cantidad)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad)
        """, (event_id, donation_id, cantidad))
    conn.commit()
    return True, "OK"


def remove_donation_from_event(conn, event_id: int, donation_id: int):
    with conn.cursor() as cur:
        cur.execute("""
            DELETE FROM evento_donacion
            WHERE evento_id=%s AND donacion_id=%s
        """, (event_id, donation_id))
    conn.commit()
    return True, "OK"


def list_donations_by_event(conn, event_id: int):
    with conn.cursor() as cur:
        cur.execute("""
            SELECT donacion_id, COALESCE(cantidad,0) AS cantidad
            FROM evento_donacion
            WHERE evento_id=%s
            ORDER BY donacion_id
        """, (event_id,))
        rows = cur.fetchall()
    # Soporta tanto DictCursor como cursor por defecto:
    result = []
    for r in rows:
        if isinstance(r, dict):
            result.append({"donacion_id": r["donacion_id"], "cantidad": r["cantidad"]})
        else:
            result.append({"donacion_id": r[0], "cantidad": r[1]})
    return result



>>>>>>> Stashed changes
