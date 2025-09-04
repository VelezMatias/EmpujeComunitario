from datetime import datetime, timezone
from sqlalchemy.orm import Session
from sqlalchemy import and_
from app.db import SessionLocal
from app import models
import ong_pb2 as pb
import ong_pb2_grpc as rpc

def _require_coord_or_presidente(auth: pb.AuthContext):
    if auth.actor_role not in (pb.PRESIDENTE, pb.COORDINADOR):
        raise PermissionError("Se requiere PRESIDENTE o COORDINADOR para gestionar eventos.")

def _is_future(dt_utc: datetime) -> bool:
    return dt_utc > datetime.now(timezone.utc)

def _parse_iso8601(s: str) -> datetime:
    # asume string ISO8601 con timezone (o Z). Para simplificar, usamos fromisoformat si viene con "+00:00".
    try:
        # Permite formatos como '2025-09-01T17:00:00+00:00' o '2025-09-01T17:00:00Z'
        if s.endswith('Z'):
            s = s[:-1] + '+00:00'
        dt = datetime.fromisoformat(s)
        if dt.tzinfo is None:
            # si no trae tz, lo consideramos UTC
            dt = dt.replace(tzinfo=timezone.utc)
        return dt.astimezone(timezone.utc)
    except Exception:
        raise ValueError("Formato de fecha inválido. Use ISO8601, ej: 2025-09-01T17:00:00Z")

class EventServiceServicer(rpc.EventServiceServicer):
    def CreateEvent(self, request: pb.CreateEventRequest, context):
        try:
            _require_coord_or_presidente(request.auth)
            dt = _parse_iso8601(request.fecha_hora)
            if not _is_future(dt):
                return pb.ApiResponse(success=False, message="La fecha/hora debe ser a futuro.")
            with SessionLocal() as db:
                ev = models.Event(nombre=request.nombre, descripcion=request.descripcion, fecha_hora=dt)
                db.add(ev)
                db.commit()
            return pb.ApiResponse(success=True, message="Evento creado.")
        except (PermissionError, ValueError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def UpdateEvent(self, request: pb.UpdateEventRequest, context):
        try:
            _require_coord_or_presidente(request.auth)
            with SessionLocal() as db:
                ev = db.query(models.Event).get(request.id)
                if not ev:
                    return pb.ApiResponse(success=False, message="Evento no encontrado.")
                # Se puede editar nombre/desc/fecha. Si se modifica a pasado, no permitir
                if request.nombre:
                    ev.nombre = request.nombre
                if request.descripcion:
                    ev.descripcion = request.descripcion
                if request.fecha_hora:
                    dt = _parse_iso8601(request.fecha_hora)
                    if not _is_future(dt):
                        return pb.ApiResponse(success=False, message="La nueva fecha/hora debe ser a futuro.")
                    ev.fecha_hora = dt
                db.commit()
            return pb.ApiResponse(success=True, message="Evento actualizado.")
        except (PermissionError, ValueError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def DeleteEvent(self, request: pb.DeleteEventRequest, context):
        try:
            _require_coord_or_presidente(request.auth)
            with SessionLocal() as db:
                ev = db.query(models.Event).get(request.id)
                if not ev:
                    return pb.ApiResponse(success=False, message="Evento no encontrado.")
                if not _is_future(ev.fecha_hora):
                    return pb.ApiResponse(success=False, message="Solo se pueden eliminar eventos a futuro.")
                db.delete(ev)
                db.commit()
            return pb.ApiResponse(success=True, message="Evento eliminado.")
        except (PermissionError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def AssignMember(self, request: pb.AssignMemberRequest, context):
        try:
            # PRESIDENTE o COORDINADOR pueden asignar cualquiera; VOLUNTARIO debería asignarse a sí mismo.
            if request.auth.actor_role not in (pb.PRESIDENTE, pb.COORDINADOR, pb.VOLUNTARIO):
                return pb.ApiResponse(success=False, message="Rol no autorizado.")
            with SessionLocal() as db:
                ev = db.query(models.Event).get(request.event_id)
                if not ev:
                    return pb.ApiResponse(success=False, message="Evento no encontrado.")
                # Si VOLUNTARIO, solo puede agregarse a sí mismo.
                if request.auth.actor_role == pb.VOLUNTARIO and request.user_id != request.auth.actor_id:
                    return pb.ApiResponse(success=False, message="VOLUNTARIO solo puede agregarse a sí mismo.")
                # Solo miembros activos son elegibles (chequeo básico)
                user = db.query(models.User).get(request.user_id)
                if not user or not user.activo:
                    return pb.ApiResponse(success=False, message="Miembro no elegible.")
                # Evitar duplicados
                exists = db.query(models.EventMember).filter_by(event_id=ev.id, user_id=user.id).first()
                if exists:
                    return pb.ApiResponse(success=False, message="Miembro ya asignado.")
                db.add(models.EventMember(event_id=ev.id, user_id=user.id))
                db.commit()
            return pb.ApiResponse(success=True, message="Miembro asignado.")
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def RemoveMember(self, request: pb.RemoveMemberRequest, context):
        try:
            if request.auth.actor_role not in (pb.PRESIDENTE, pb.COORDINADOR, pb.VOLUNTARIO):
                return pb.ApiResponse(success=False, message="Rol no autorizado.")
            with SessionLocal() as db:
                ev = db.query(models.Event).get(request.event_id)
                if not ev:
                    return pb.ApiResponse(success=False, message="Evento no encontrado.")
                # VOLUNTARIO solo puede quitarse a sí mismo.
                if request.auth.actor_role == pb.VOLUNTARIO and request.user_id != request.auth.actor_id:
                    return pb.ApiResponse(success=False, message="VOLUNTARIO solo puede quitarse a sí mismo.")
                link = db.query(models.EventMember).filter_by(event_id=request.event_id, user_id=request.user_id).first()
                if not link:
                    return pb.ApiResponse(success=False, message="Miembro no estaba asignado.")
                db.delete(link)
                db.commit()
            return pb.ApiResponse(success=True, message="Miembro quitado.")
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def RegisterDistribution(self, request: pb.RegisterDistributionRequest, context):
        try:
            # Solo PRESIDENTE o COORDINADOR pueden registrar la distribución post-evento
            if request.auth.actor_role not in (pb.PRESIDENTE, pb.COORDINADOR):
                return pb.ApiResponse(success=False, message="No autorizado.")
            with SessionLocal() as db:
                ev = db.query(models.Event).get(request.event_id)
                if not ev:
                    return pb.ApiResponse(success=False, message="Evento no encontrado.")
                # Debe tratarse de un evento pasado
                if _is_future(ev.fecha_hora):
                    return pb.ApiResponse(success=False, message="Solo se registra distribución en eventos pasados.")
                # Descontar inventario
                for d in request.dist:
                    if d.cantidad <= 0:
                        return pb.ApiResponse(success=False, message="Cantidad debe ser > 0.")
                    item = db.query(models.DonationItem).get(d.donation_item_id)
                    if not item or item.eliminado:
                        return pb.ApiResponse(success=False, message=f"Ítem {d.donation_item_id} no válido.")
                    if item.cantidad < d.cantidad:
                        return pb.ApiResponse(success=False, message=f"Stock insuficiente para ítem {item.id}.")
                # Si todo ok, aplicar
                for d in request.dist:
                    item = db.query(models.DonationItem).get(d.donation_item_id)
                    item.cantidad -= d.cantidad
                    db.add(models.EventDonationDistribution(
                        event_id=ev.id, donation_item_id=item.id, cantidad=d.cantidad
                    ))
                db.commit()
            return pb.ApiResponse(success=True, message="Distribución registrada y stock actualizado.")
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def ListEvents(self, request: pb.Empty, context):
        try:
            with SessionLocal() as db:
                events = db.query(models.Event).all()
                items = []
                for ev in events:
                    miembros = [em.user_id for em in ev.miembros]
                    items.append(pb.Event(
                        id=ev.id, nombre=ev.nombre, descripcion=ev.descripcion,
                        fecha_hora=ev.fecha_hora.astimezone(timezone.utc).isoformat(),
                        miembros=miembros
                    ))
                return pb.ListEventsResponse(events=items)
        except Exception:
            return pb.ListEventsResponse(events=[])