from datetime import datetime, timezone
from sqlalchemy.orm import Session
from app.db import SessionLocal
from app import models
from app.security import hash_password, verify_password, generate_random_password
from app.emailer import send_password_email

import ong_pb2 as pb
import ong_pb2_grpc as rpc

def _require_presidente(auth: pb.AuthContext):
    if auth.actor_role != pb.PRESIDENTE:
        raise PermissionError("Solo PRESIDENTE puede gestionar usuarios.")

def _role_from_pb(role_enum: pb.Role) -> models.RoleEnum:
    mapping = {
        pb.PRESIDENTE: models.RoleEnum.PRESIDENTE,
        pb.VOCAL: models.RoleEnum.VOCAL,
        pb.COORDINADOR: models.RoleEnum.COORDINADOR,
        pb.VOLUNTARIO: models.RoleEnum.VOLUNTARIO,
    }
    return mapping.get(role_enum, models.RoleEnum.VOLUNTARIO)

class UserServiceServicer(rpc.UserServiceServicer):
    def __init__(self):
        pass

    def CreateUser(self, request: pb.CreateUserRequest, context):
        try:
            _require_presidente(request.auth)
            with SessionLocal() as db:
                if db.query(models.User).filter_by(username=request.username).first():
                    return pb.ApiResponse(success=False, message="Nombre de usuario ya existe.")
                if db.query(models.User).filter_by(email=request.email).first():
                    return pb.ApiResponse(success=False, message="Email ya existe.")

                password = generate_random_password()
                user = models.User(
                    username=request.username,
                    nombre=request.nombre,
                    apellido=request.apellido,
                    telefono=request.telefono,
                    email=request.email,
                    rol=_role_from_pb(request.rol),
                    activo=True,
                    password_hash=hash_password(password),
                )
                db.add(user)
                db.commit()

                # “Enviar” por email (stub)
                send_password_email(user.email, password)

                return pb.ApiResponse(success=True, message="Usuario creado y contraseña enviada por email.")
        except PermissionError as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def UpdateUser(self, request: pb.UpdateUserRequest, context):
        try:
            _require_presidente(request.auth)
            with SessionLocal() as db:
                user = db.query(models.User).get(request.id)
                if not user:
                    return pb.ApiResponse(success=False, message="Usuario no encontrado.")
                # No se permite cambiar contraseña acá
                user.nombre = request.nombre or user.nombre
                user.apellido = request.apellido or user.apellido
                user.telefono = request.telefono or user.telefono
                user.email = request.email or user.email
                user.rol = _role_from_pb(request.rol) if request.rol != pb.ROLE_UNSPECIFIED else user.rol
                user.activo = request.activo if request.activo in [True, False] else user.activo
                db.commit()
                return pb.ApiResponse(success=True, message="Usuario actualizado.")
        except PermissionError as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def DeactivateUser(self, request: pb.DeactivateUserRequest, context):
        try:
            _require_presidente(request.auth)
            with SessionLocal() as db:
                user = db.query(models.User).get(request.id)
                if not user:
                    return pb.ApiResponse(success=False, message="Usuario no encontrado.")
                user.activo = False
                db.commit()
                return pb.ApiResponse(success=True, message="Usuario desactivado.")
        except PermissionError as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def Login(self, request: pb.LoginRequest, context):
        try:
            with SessionLocal() as db:
                q = db.query(models.User)
                user = q.filter(
                    (models.User.username == request.username_or_email) |
                    (models.User.email == request.username_or_email)
                ).first()
                if not user:
                    return pb.LoginResponse(success=False, message="Usuario/Email inexistente.")

                if not user.activo:
                    return pb.LoginResponse(success=False, message="Usuario inactivo.")

                if not verify_password(request.password, user.password_hash):
                    return pb.LoginResponse(success=False, message="Credenciales incorrectas.")

                role_map = {
                    models.RoleEnum.PRESIDENTE: pb.PRESIDENTE,
                    models.RoleEnum.VOCAL: pb.VOCAL,
                    models.RoleEnum.COORDINADOR: pb.COORDINADOR,
                    models.RoleEnum.VOLUNTARIO: pb.VOLUNTARIO,
                }
                return pb.LoginResponse(
                    success=True,
                    message="Login ok.",
                    user_id=user.id,
                    rol=role_map[user.rol],
                )
        except Exception as e:
            return pb.LoginResponse(success=False, message=f"Error: {e}")

    def ListUsers(self, request: pb.Empty, context):
        try:
            with SessionLocal() as db:
                users = db.query(models.User).all()
                role_map = {
                    models.RoleEnum.PRESIDENTE: pb.PRESIDENTE,
                    models.RoleEnum.VOCAL: pb.VOCAL,
                    models.RoleEnum.COORDINADOR: pb.COORDINADOR,
                    models.RoleEnum.VOLUNTARIO: pb.VOLUNTARIO,
                }
                return pb.ListUsersResponse(users=[
                    pb.User(
                        id=u.id, username=u.username, nombre=u.nombre, apellido=u.apellido,
                        telefono=u.telefono or "", email=u.email, rol=role_map[u.rol], activo=u.activo
                    ) for u in users
                ])
        except Exception:
            return pb.ListUsersResponse(users=[])