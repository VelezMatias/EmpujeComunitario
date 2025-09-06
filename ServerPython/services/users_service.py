# services/users_service.py
# Implementación usando MySQL real.
# - Valida username/password contra la tabla 'usuarios'.
# - Sólo autentica usuarios con 'activo = 1'.
# - Devuelve los campos de 'usuarios' según tu users.proto.

import users_pb2
import users_pb2_grpc

from db import get_connection  # importamos nuestro módulo de BD

class UsersService(users_pb2_grpc.UsersServiceServicer):
    def Login(self, request, context):
        """
        Autentica contra la tabla 'usuarios':
        - username (UNIQUE)
        - password (texto plano en tu dump)
        - activo = 1
        """
        username = (request.username or "").strip()
        password = (request.password or "").strip()

        if not username or not password:
            return users_pb2.LoginResponse(ok=False, mensaje="Faltan credenciales")

        try:
            conn = get_connection()
            try:
                with conn.cursor(dictionary=True) as cur:
                    # Consulta segura (parametrizada) y filtrando activo = 1
                    cur.execute(
                        """
                        SELECT id, username, nombre, apellido, telefono, password, email, rol_id, activo
                        FROM usuarios
                        WHERE username = %s AND password = %s AND activo = 1
                        LIMIT 1
                        """,
                        (username, password)
                    )
                    row = cur.fetchone()

                if not row:
                    return users_pb2.LoginResponse(ok=False, mensaje="Credenciales inválidas o usuario inactivo")

                # Mapeamos columnas -> mensaje User del .proto
                user_msg = users_pb2.User(
                    id        = int(row["id"]),
                    username  = row["username"] or "",
                    nombre    = row["nombre"] or "",
                    apellido  = row["apellido"] or "",
                    email     = row["email"] or "",
                    telefono  = row["telefono"] or "",
                    rol_id    = int(row["rol_id"]) if row["rol_id"] is not None else 0,
                    activo    = bool(row["activo"])
                )

                return users_pb2.LoginResponse(ok=True, mensaje="OK", user=user_msg)

            finally:
                conn.close()

        except Exception as e:
            # Log del lado servidor para diagnosticar
            print(f"[gRPC][Python] Error en Login (BD): {e}")
            # INTERNAL
            context.set_code(13)  # grpc.StatusCode.INTERNAL
            context.set_details("Error interno en Login (BD)")
            return users_pb2.LoginResponse(ok=False, mensaje="Error interno")
