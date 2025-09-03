from datetime import datetime, timezone
from sqlalchemy.orm import Session
from app.db import SessionLocal
from app import models
import ong_pb2 as pb
import ong_pb2_grpc as rpc

def _ensure_non_negative(n: int):
    if n is not None and n < 0:
        raise ValueError("La cantidad no puede ser negativa.")

def _require_vocal_or_presidente(auth: pb.AuthContext):
    if auth.actor_role not in (pb.PRESIDENTE, pb.VOCAL):
        raise PermissionError("Se requiere PRESIDENTE o VOCAL para gestionar inventario.")

def _cat_from_pb(c: pb.Category) -> models.CategoryEnum:
    mapping = {
        pb.ROPA: models.CategoryEnum.ROPA,
        pb.ALIMENTOS: models.CategoryEnum.ALIMENTOS,
        pb.JUGUETES: models.CategoryEnum.JUGUETES,
        pb.UTILES_ESCOLARES: models.CategoryEnum.UTILES_ESCOLARES,
    }
    return mapping.get(c, models.CategoryEnum.ROPA)

class DonationServiceServicer(rpc.DonationServiceServicer):
    def CreateDonationItem(self, request: pb.CreateDonationRequest, context):
        try:
            _require_vocal_or_presidente(request.auth)
            _ensure_non_negative(request.cantidad)
            with SessionLocal() as db:
                item = models.DonationItem(
                    categoria=_cat_from_pb(request.categoria),
                    descripcion=request.descripcion,
                    cantidad=request.cantidad,
                    created_by=request.auth.actor_id if request.auth.actor_id else None,
                    updated_by=request.auth.actor_id if request.auth.actor_id else None,
                )
                db.add(item)
                db.commit()
            return pb.ApiResponse(success=True, message="Ítem de donación creado.")
        except (PermissionError, ValueError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def UpdateDonationItem(self, request: pb.UpdateDonationRequest, context):
        try:
            _require_vocal_or_presidente(request.auth)
            if request.cantidad is not None:
                _ensure_non_negative(request.cantidad)

            with SessionLocal() as db:
                item = db.query(models.DonationItem).get(request.id)
                if not item or item.eliminado:
                    return pb.ApiResponse(success=False, message="Ítem no encontrado o eliminado.")
                if request.descripcion:
                    item.descripcion = request.descripcion
                if request.cantidad is not None:
                    item.cantidad = request.cantidad
                item.updated_by = request.auth.actor_id if request.auth.actor_id else item.updated_by
                db.commit()
            return pb.ApiResponse(success=True, message="Ítem actualizado.")
        except (PermissionError, ValueError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def SoftDeleteDonationItem(self, request: pb.SoftDeleteDonationRequest, context):
        try:
            _require_vocal_or_presidente(request.auth)
            with SessionLocal() as db:
                item = db.query(models.DonationItem).get(request.id)
                if not item or item.eliminado:
                    return pb.ApiResponse(success=False, message="Ítem no encontrado o ya eliminado.")
                item.eliminado = True
                item.updated_by = request.auth.actor_id if request.auth.actor_id else item.updated_by
                db.commit()
            return pb.ApiResponse(success=True, message="Baja lógica aplicada.")
        except (PermissionError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            return pb.ApiResponse(success=False, message=f"Error: {e}")

    def ListDonationItems(self, request: pb.Empty, context):
        try:
            with SessionLocal() as db:
                items = db.query(models.DonationItem).filter_by(eliminado=False).all()
                cat_map = {
                    models.CategoryEnum.ROPA: pb.ROPA,
                    models.CategoryEnum.ALIMENTOS: pb.ALIMENTOS,
                    models.CategoryEnum.JUGUETES: pb.JUGUETES,
                    models.CategoryEnum.UTILES_ESCOLARES: pb.UTILES_ESCOLARES,
                }
                def iso(dt): return dt.astimezone(timezone.utc).isoformat() if dt else ""
                return pb.ListDonationsResponse(items=[
                    pb.DonationItem(
                        id=i.id, categoria=cat_map[i.categoria], descripcion=i.descripcion,
                        cantidad=i.cantidad, eliminado=i.eliminado,
                        created_at=iso(i.created_at), created_by=i.created_by or 0,
                        updated_at=iso(i.updated_at), updated_by=i.updated_by or 0
                    ) for i in items
                ])
        except Exception:
            return pb.ListDonationsResponse(items=[])