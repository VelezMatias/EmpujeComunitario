# ServerPython/app/services/event_service.py
from datetime import datetime, timezone
from typing import List

from app import models
import ong_pb2 as pb
import ong_pb2_grpc as rpc


# === Helpers de roles/fechas ===

def _require_coord_o_presidente(auth: pb.AuthContext):
    if auth.actor_role not in (pb.PRESIDENTE, pb.COORDINADOR):
        raise PermissionError("Se requiere PRESIDENTE o COORDINADOR para gestionar eventos.")

def _is_future(dt_utc: datetime) -> bool:
    return dt_utc > datetime.now(timezone.utc)

def _parse_iso8601_utc(s: str) -> datetime:
    """
    Acepta '2025-09-01T17:00:00Z' o '2025-09-01T17:00:00+00:00'.
    Si no trae tz, se asume UTC.
    """
    try:
        if s.endswith('Z'):
            s = s[:-1] + '+00:00'
        dt = datetime.fromisoformat(s)
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
        return dt.astimezone(timezone.utc)
    except Exception:
        raise ValueError("Formato de fecha inválido. Use ISO8601, ej: 2025-09-01T17:00:00Z")


class EventServiceServicer(rpc.EventServiceServicer):
    """
    Implementación sin ORM, delegando todo a app.models (consultas directas).
    Asegurate de exponer en models las funciones listadas abajo.
    """

    # --------- CREATE ---------
    def CreateEvent(self, request: pb.CreateEventRequest, context):
        try:
            _require_coord_o_presidente(request.auth)

            if not request.nombre.strip():
                return pb.ApiResponse(success=False, message="El nombre es obligatorio.")

            dt = _parse_iso8601_utc(request.fecha_hora)
            if not _is_future(dt):
                return pb.ApiResponse(success=False, message="La fecha/hora debe ser a futuro.")

            # models.crear_evento(nombre:str, descripcion:str, fecha_utc:datetime) -> int (id nuevo)
            new_id = models.crear_evento(
                nombre=request.nombre.strip(),
                descripcion=(request.descripcion or "").strip(),
                fecha_utc=dt,
                creador_id=request.auth.actor_id,
            )

            return pb.ApiResponse(success=True, message=f"Evento creado (id={new_id}).")

        except (PermissionError, ValueError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    # --------- UPDATE ---------
    def UpdateEvent(self, request: pb.UpdateEventRequest, context):
        try:
            _require_coord_o_presidente(request.auth)

            # models.evento_por_id(id:int) -> dict|None  (keys: id, nombre, descripcion, fecha_hora[datetime UTC])
            ev = models.evento_por_id(request.id)
            if not ev:
                return pb.ApiResponse(success=False, message="Evento no encontrado.")

            nombre = request.nombre.strip() if request.nombre else None
            descripcion = request.descripcion.strip() if request.descripcion else None

            nueva_fecha = None
            if request.fecha_hora:
                dt = _parse_iso8601_utc(request.fecha_hora)
                if not _is_future(dt):
                    return pb.ApiResponse(success=False, message="La nueva fecha/hora debe ser a futuro.")
                nueva_fecha = dt

            # models.actualizar_evento(id:int, nombre:str|None, desc:str|None, fecha_utc:datetime|None) -> int rows
            rows = models.actualizar_evento(
                id_evento=request.id,
                nombre=nombre,
                descripcion=descripcion,
                fecha_utc=nueva_fecha
            )
            if rows == 0:
                return pb.ApiResponse(success=False, message="Sin cambios o evento no encontrado.")

            return pb.ApiResponse(success=True, message="Evento actualizado.")

        except (PermissionError, ValueError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    # --------- DELETE ---------
    def DeleteEvent(self, request: pb.DeleteEventRequest, context):
        try:
            _require_coord_o_presidente(request.auth)

            ev = models.evento_por_id(request.id)
            if not ev:
                return pb.ApiResponse(success=False, message="Evento no encontrado.")

            # ev["fecha_hora"] debe ser datetime con tz UTC; si fuera string, parsealo igual que arriba
            fecha: datetime = ev["fecha_hora"]
            if fecha.tzinfo is None:
                fecha = fecha.replace(tzinfo=timezone.utc)
            if not _is_future(fecha):
                return pb.ApiResponse(success=False, message="Solo se pueden eliminar eventos a futuro.")

            # models.eliminar_evento(id:int) -> int rows
            rows = models.eliminar_evento(request.id)
            if rows == 0:
                return pb.ApiResponse(success=False, message="No se pudo eliminar.")
            return pb.ApiResponse(success=True, message="Evento eliminado.")

        except (PermissionError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    # --------- ASSIGN ---------
    def AssignMember(self, request: pb.AssignMemberRequest, context):
        try:
            # PRESIDENTE/COORDINADOR pueden asignar cualquier usuario; VOLUNTARIO solo a sí mismo
            rol = request.auth.actor_role
            if rol not in (pb.PRESIDENTE, pb.COORDINADOR, pb.VOLUNTARIO):
                return pb.ApiResponse(success=False, message="Rol no autorizado.")

            ev = models.evento_por_id(request.event_id)
            if not ev:
                return pb.ApiResponse(success=False, message="Evento no encontrado.")

            # VOLUNTARIO solo se asigna a sí mismo
            if rol == pb.VOLUNTARIO and request.user_id != request.auth.actor_id:
                return pb.ApiResponse(success=False, message="VOLUNTARIO solo puede agregarse a sí mismo.")

            # models.usuario_por_id(id:int) -> dict|None (keys: id, activo:int/bool)
            u = models.usuario_por_id(request.user_id)
            if not u or not bool(u["activo"]):
                return pb.ApiResponse(success=False, message="Miembro no elegible.")

            # models.evento_tiene_miembro(event_id:int, user_id:int) -> bool
            if models.evento_tiene_miembro(request.event_id, request.user_id):
                return pb.ApiResponse(success=False, message="Miembro ya asignado.")

            # models.agregar_miembro_evento(event_id:int, user_id:int) -> int rows
            models.agregar_miembro_evento(request.event_id, request.user_id)
            return pb.ApiResponse(success=True, message="Miembro asignado.")

        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    # --------- REMOVE ---------
    def RemoveMember(self, request: pb.RemoveMemberRequest, context):
        try:
            rol = request.auth.actor_role
            if rol not in (pb.PRESIDENTE, pb.COORDINADOR, pb.VOLUNTARIO):
                return pb.ApiResponse(success=False, message="Rol no autorizado.")

            ev = models.evento_por_id(request.event_id)
            if not ev:
                return pb.ApiResponse(success=False, message="Evento no encontrado.")

            # VOLUNTARIO solo puede quitarse a sí mismo
            if rol == pb.VOLUNTARIO and request.user_id != request.auth.actor_id:
                return pb.ApiResponse(success=False, message="VOLUNTARIO solo puede quitarse a sí mismo.")

            # models.quitar_miembro_evento(event_id:int, user_id:int) -> int rows
            rows = models.quitar_miembro_evento(request.event_id, request.user_id)
            if rows == 0:
                return pb.ApiResponse(success=False, message="Miembro no estaba asignado.")
            return pb.ApiResponse(success=True, message="Miembro quitado.")

        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    # --------- REGISTER DISTRIBUTION ---------
    def RegisterDistribution(self, request: pb.RegisterDistributionRequest, context):
        try:
            if request.auth.actor_role not in (pb.PRESIDENTE, pb.COORDINADOR):
                return pb.ApiResponse(success=False, message="No autorizado.")

            ev = models.evento_por_id(request.event_id)
            if not ev:
                return pb.ApiResponse(success=False, message="Evento no encontrado.")

            fecha: datetime = ev["fecha_hora"]
            if fecha.tzinfo is None:
                fecha = fecha.replace(tzinfo=timezone.utc)
            if _is_future(fecha):
                return pb.ApiResponse(success=False, message="Solo se registra distribución en eventos pasados.")

            # Validaciones de stock (primero chequeo, luego aplico)
            for d in request.dist:
                if d.cantidad <= 0:
                    return pb.ApiResponse(success=False, message="Cantidad debe ser > 0.")
                item = models.item_por_id(d.donation_item_id)  # keys: id, cantidad, eliminado
                if not item or bool(item.get("eliminado", 0)):
                    return pb.ApiResponse(success=False, message=f"Ítem {d.donation_item_id} no válido.")
                if int(item["cantidad"]) < int(d.cantidad):
                    return pb.ApiResponse(success=False, message=f"Stock insuficiente para ítem {item['id']}.")

            # Aplicar: descontar y registrar
            for d in request.dist:
                models.descontar_stock_item(d.donation_item_id, d.cantidad)
                models.insertar_distribucion_evento(
                    event_id=request.event_id,
                    donation_item_id=d.donation_item_id,
                    cantidad=d.cantidad
                )

            return pb.ApiResponse(success=True, message="Distribución registrada y stock actualizado.")

        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")



    def ListEvents(self, request, context):
        try:
            filas = models.listar_eventos_con_miembros()
            eventos = []
            for r in filas:
                eventos.append(pb.Event(
                    id=r["id"],
                    nombre=r.get("nombre") or "",
                    descripcion=r.get("descripcion") or "",
                    fecha_hora=_to_iso_utc_safe(r.get("fecha_hora")),
                    miembros=r.get("miembros", []),
                    creador_id=int(r.get("creador_id") or 0),
                ))
            return pb.ListEventsResponse(events=eventos)
        except Exception as e:
            print("[EVENTOS][List][ERR]", e)
            # Fallback: al menos devolver eventos sin miembros
            filas = models.listar_eventos()
            eventos = []
            for r in filas:
                eventos.append(pb.Event(
                    id=r["id"],
                    nombre=r.get("nombre") or "",
                    descripcion=r.get("descripcion") or "",
                    fecha_hora=_to_iso_utc_safe(r.get("fecha_hora")),
                    miembros=[],
                    creador_id=int(r.get("creador_id") or 0),
                ))
            return pb.ListEventsResponse(events=eventos)
            


    
def _to_iso_utc_safe(fecha):
    if not fecha:
        return ""
    try:
        if isinstance(fecha, str):
            try:
                fecha = datetime.strptime(fecha, '%Y-%m-%d %H:%M:%S')
            except Exception:
                s = fecha[:-1] + '+00:00' if fecha.endswith('Z') else fecha
                fecha = datetime.fromisoformat(s)
        if fecha.tzinfo is None:
            fecha = fecha.replace(tzinfo=timezone.utc)
        else:
            fecha = fecha.astimezone(timezone.utc)
        return fecha.isoformat()
    except Exception:
        return ""