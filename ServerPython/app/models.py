"""
DAO sin ORM para esquema MySQL con tablas:
  - usuarios, roles, eventos, donaciones, evento_participantes, categorias
De momento implementamos usuarios/roles (login/registro).
"""

from typing import Optional, List, Dict
from datetime import datetime
from app.db import fetch_one, fetch_all, execute

import os

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




_SCHEMA = os.getenv("DB_NAME", "").strip()
T_EVENTOS = f"`{_SCHEMA}`.`eventos`" if _SCHEMA else "eventos"
T_EP      = f"`{_SCHEMA}`.`evento_participantes`" if _SCHEMA else "evento_participantes"

def listar_eventos():
    sql = f"""
      SELECT id, nombre, descripcion, fecha_hora, creador_id
      FROM {T_EVENTOS}
      ORDER BY fecha_hora DESC, id DESC
    """
    return fetch_all(sql)

def listar_eventos_con_miembros():
    """
    Devuelve eventos + lista de usuario_id por evento.
    """
    eventos = listar_eventos()
    if not eventos:
        return []

    ids = [e["id"] for e in eventos]
    # Si no hay ids, devolvemos vacío
    if not ids:
        return []

    rows = []
    try:
        placeholders = ",".join(["%s"] * len(ids))
        # ¡OJO a los nombres reales de columnas: evento_id, usuario_id!
        sql = f"""
          SELECT ep.evento_id, ep.usuario_id
          FROM {T_EP} ep
          WHERE ep.evento_id IN ({placeholders})
          ORDER BY ep.evento_id ASC
        """
        rows = fetch_all(sql, tuple(ids))
    except Exception as e:
        print("[MODELS][listar_eventos_con_miembros][WARN]", e)
        rows = []

    por_evento = {}
    for r in rows or []:
        por_evento.setdefault(r["evento_id"], []).append(r["usuario_id"])

    for e in eventos:
        e["miembros"] = por_evento.get(e["id"], [])
    return eventos


def miembros_ids_por_evento(evento_id: int):
    sql = """
        SELECT ep.usuario_id
        FROM evento_participantes ep
        JOIN usuarios u ON u.id = ep.usuario_id
        WHERE ep.evento_id = %s
          AND u.activo = 1
    """
    rows = fetch_all(sql, (evento_id,))
    return [r["usuario_id"] for r in rows] if rows else []

def evento_tiene_miembro(event_id: int, user_id: int) -> bool:
    sql = "SELECT 1 AS ok FROM evento_participantes WHERE evento_id=%s AND usuario_id=%s LIMIT 1"
    return fetch_one(sql, (event_id, user_id)) is not None

def agregar_miembro_evento(event_id: int, user_id: int) -> int:
    # Opción robusta sin requerir índice único (evita duplicados):
    sql = """
        INSERT INTO evento_participantes (evento_id, usuario_id)
        SELECT %s, %s
        WHERE NOT EXISTS (
            SELECT 1 FROM evento_participantes
            WHERE evento_id=%s AND usuario_id=%s
        )
    """
    rowcount, _ = execute(sql, (event_id, user_id, event_id, user_id))
    return rowcount

def quitar_miembro_evento(event_id: int, user_id: int) -> int:
    sql = "DELETE FROM evento_participantes WHERE evento_id=%s AND usuario_id=%s"
    rowcount, _ = execute(sql, (event_id, user_id))
    return rowcount

def usuario_por_id(id_usuario: int):
    sql = "SELECT id, activo FROM usuarios WHERE id = %s LIMIT 1"
    return fetch_one(sql, (id_usuario,))
