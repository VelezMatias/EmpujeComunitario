from app import models
from app.security import hash_password, verify_password, generate_random_password
import ong_pb2 as pb
import ong_pb2_grpc as rpc

# Map enums gRPC <-> rol_id BD (ajusta si tus ids difieren)
_PB_TO_ROL_ID = {
    pb.PRESIDENTE: 1,
    pb.VOCAL: 2,
    pb.COORDINADOR: 3,
    pb.VOLUNTARIO: 4,
}
_ROL_ID_TO_PB = {v: k for k, v in _PB_TO_ROL_ID.items()}

def _require_presidente(auth: pb.AuthContext):
    if auth.actor_role != pb.PRESIDENTE:
        raise PermissionError("Solo PRESIDENTE puede gestionar usuarios.")

class UserServiceServicer(rpc.UserServiceServicer):
    def CreateUser(self, request: pb.CreateUserRequest, context):
        try:
            _require_presidente(request.auth)

            if models.usuario_por_username(request.username):
                return pb.ApiResponse(success=False, message="Nombre de usuario ya existe.")
            if models.usuario_por_email(request.email):
                return pb.ApiResponse(success=False, message="Email ya existe.")

            rol_id = _PB_TO_ROL_ID.get(request.rol, 4)
            if not models.rol_existe(rol_id):
                return pb.ApiResponse(success=False, message="Rol inexistente en la BD.")

            # Generar y guardar password en hash
            plain = generate_random_password()
            pwd_hash = hash_password(plain)

            new_id = models.crear_usuario(
                username=request.username,
                nombre=request.nombre,
                apellido=request.apellido,
                telefono=request.telefono,
                email=request.email,
                password_hash=pwd_hash,  # se guarda en 'password'
                rol_id=rol_id,
                activo=1,
            )

            print("[DEBUG] Alta usuario")
            print(f"        id: {new_id}")
            print(f"        username: {request.username}")
            print(f"        email   : {request.email}")
            print(f"        pass    : {plain}")

            return pb.ApiResponse(success=True, message="Usuario creado. Contraseña generada y registrada.")
        except PermissionError as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def UpdateUser(self, request: pb.UpdateUserRequest, context):
        try:
            _require_presidente(request.auth)
            rol_id = None if request.rol == pb.ROLE_UNSPECIFIED else _PB_TO_ROL_ID.get(request.rol, 4)
            if rol_id is not None and not models.rol_existe(rol_id):
                return pb.ApiResponse(success=False, message="Rol inexistente en la BD.")

            rows = models.actualizar_usuario(
                id_usuario=request.id,
                nombre=request.nombre if request.nombre else None,
                apellido=request.apellido if request.apellido else None,
                telefono=request.telefono if request.telefono else None,
                email=request.email if request.email else None,
                rol_id=rol_id,
                activo=request.activo if request.activo in [True, False] else None
            )
            if rows == 0:
                return pb.ApiResponse(success=False, message="Sin cambios o usuario no encontrado.")
            return pb.ApiResponse(success=True, message="Usuario actualizado.")
        except PermissionError as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def DeactivateUser(self, request: pb.DeactivateUserRequest, context):
        try:
            _require_presidente(request.auth)
            rows = models.desactivar_usuario(request.id)
            if rows == 0:
                return pb.ApiResponse(success=False, message="Usuario no encontrado.")
            return pb.ApiResponse(success=True, message="Usuario desactivado.")
        except PermissionError as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def Login(self, request: pb.LoginRequest, context):
        try:
            u = models.usuario_por_identifier(request.username_or_email)
            if not u:
                return pb.LoginResponse(success=False, message="Usuario/Email inexistente.")
            if not bool(u["activo"]):
                return pb.LoginResponse(success=False, message="Usuario inactivo.")
            if not verify_password(request.password, u["password"]):
                return pb.LoginResponse(success=False, message="Credenciales incorrectas.")

            return pb.LoginResponse(
                success=True,
                message="Login ok.",
                user_id=u["id"],
                rol=_ROL_ID_TO_PB.get(u["rol_id"], pb.VOLUNTARIO),
            )
        except Exception as e:
            return pb.LoginResponse(success=False, message=f"Error: {e}")

    def ListUsers(self, request: pb.Empty, context):
        try:
            rows = models.listar_usuarios()
            users = []
            for r in rows:
                users.append(pb.User(
                    id=r["id"],
                    username=r["username"],
                    nombre=r["nombre"],
                    apellido=r["apellido"],
                    telefono=r.get("telefono") or "",
                    email=r["email"],
                    rol=_ROL_ID_TO_PB.get(r["rol_id"], pb.VOLUNTARIO),
                    activo=bool(r["activo"]),
                ))
            return pb.ListUsersResponse(users=users)
        except Exception:
            return pb.ListUsersResponse(users=[])
