# services/users_service.py
# Implementación mínima y robusta del servicio de usuarios para pruebas.
# - No toca la BD.
# - Devuelve OK si username="coord1" y password="coord123".

import users_pb2
import users_pb2_grpc

class UsersService(users_pb2_grpc.UsersServiceServicer):
    def Login(self, request, context):
        try:
            username = request.username or ""
            password = request.password or ""

            if username == "coord1" and password == "coord123":
                user = users_pb2.User(
                    id=3,
                    username="coord1",
                    nombre="María",
                    apellido="Fernández",
                    email="maria@example.com",
                    telefono="11223344",
                    rol_id=3,
                    activo=True,
                )
                return users_pb2.LoginResponse(ok=True, mensaje="OK", user=user)

            return users_pb2.LoginResponse(ok=False, mensaje="Credenciales inválidas")
        except Exception as e:
            # Loguea en servidor y devuelve INTERNAL
            print(f"[gRPC][Python] Error en Login: {e}")
            context.set_code(13)  # grpc.StatusCode.INTERNAL
            context.set_details("Error interno en Login")
            return users_pb2.LoginResponse(ok=False, mensaje="Error interno")
