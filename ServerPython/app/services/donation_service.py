# ServerPython/app/services/donation_service.py
from datetime import datetime
from typing import Optional

from app.db import fetch_one, fetch_all, execute
import ong_pb2 as pb
import ong_pb2_grpc as rpc


# ===== Helpers =====

def _require_vocal_o_presidente(auth: pb.AuthContext):
    if auth.actor_role not in (pb.PRESIDENTE, pb.VOCAL):
        raise PermissionError("Se requiere PRESIDENTE o VOCAL para gestionar inventario.")

NAME_TO_ENUM = {
    "ROPA": pb.ROPA,
    "ALIMENTOS": pb.ALIMENTOS,
    "JUGUETES": pb.JUGUETES,
    "UTILES_ESCOLARES": pb.UTILES_ESCOLARES,
}
ENUM_TO_NAME = {v: k for k, v in NAME_TO_ENUM.items()}

def _to_iso_local(fecha):
    """
    Devuelve la fecha tal cual LOCAL (sin 'Z'), formato ISO compacto "YYYY-MM-DDTHH:MM:SS".
    No fuerza UTC.
    """
    if not fecha:
        return ""
    try:
        if isinstance(fecha, str):
            # Formatos comunes de MySQL
            try:
                fecha = datetime.strptime(fecha, '%Y-%m-%d %H:%M:%S')
            except Exception:
                if fecha.endswith('Z'):
                    fecha = fecha[:-1]
                fecha = datetime.fromisoformat(fecha)
        return fecha.strftime('%Y-%m-%dT%H:%M:%S')
    except Exception:
        return ""

def _categoria_id_from_enum(cat_enum: pb.Category) -> Optional[int]:
    nombre = ENUM_TO_NAME.get(cat_enum)
    if not nombre:
        return None
    row = fetch_one("SELECT id FROM categorias WHERE UPPER(nombre)=UPPER(%s) LIMIT 1", (nombre,))
    return int(row["id"]) if row else None

def _categoria_enum_from_id(cat_id: int) -> pb.Category:
    row = fetch_one("SELECT nombre FROM categorias WHERE id=%s", (int(cat_id),))
    if not row:
        return pb.CATEGORY_UNSPECIFIED
    return NAME_TO_ENUM.get((row["nombre"] or "").upper(), pb.CATEGORY_UNSPECIFIED)

def _row_to_pb(r: dict) -> pb.DonationItem:
    return pb.DonationItem(
        id=int(r["id"]),
        categoria=_categoria_enum_from_id(r["categoria_id"]),
        descripcion=r.get("descripcion") or "",
        cantidad=int(r.get("cantidad") or 0),
        eliminado=bool(r.get("eliminado") or 0),
        created_at=_to_iso_local(r.get("fecha_alta")),
        created_by=int(r.get("usuario_alta") or 0),
        updated_at=_to_iso_local(r.get("fecha_modificacion")),
        updated_by=int(r.get("usuario_modificacion") or 0),
    )


class DonationServiceServicer(rpc.DonationServiceServicer):

    # Alta
    def CreateDonationItem(self, request: pb.CreateDonationRequest, context):
        try:
            _require_vocal_o_presidente(request.auth)
            if request.categoria == pb.CATEGORY_UNSPECIFIED:
                raise ValueError("La categoría es obligatoria.")
            if (request.descripcion or "").strip() == "":
                raise ValueError("La descripción es obligatoria.")
            if request.cantidad < 0:
                raise ValueError("La cantidad no puede ser negativa.")

            # Fijamos TZ de la SESIÓN (solo para esta conexión del servicio) a -03:00
            execute("SET time_zone = '-03:00'")

            cat_id = _categoria_id_from_enum(request.categoria)
            if cat_id is None:
                raise ValueError("Categoría inexistente.")

            # Guarda hora local (NOW) en fecha_alta (TIMESTAMP) y usuario_alta
            sql = """
                INSERT INTO donaciones (categoria_id, descripcion, cantidad, eliminado, fecha_alta, usuario_alta)
                VALUES (%s, %s, %s, 0, NOW(), %s)
            """
            _, last_id = execute(sql, (cat_id, request.descripcion.strip(), int(request.cantidad), int(request.auth.actor_id)))
            return pb.ApiResponse(success=True, message=f"Donación creada (id={last_id}).")
        except (PermissionError, ValueError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            print("[DONACIONES][Create][ERR]", e)
            return pb.ApiResponse(success=False, message="Error interno al crear.")

    # Modificación (desc + cantidad) + auditoría
    def UpdateDonationItem(self, request: pb.UpdateDonationRequest, context):
        try:
            _require_vocal_o_presidente(request.auth)
            if (request.descripcion or "").strip() == "":
                raise ValueError("La descripción es obligatoria.")
            if request.cantidad < 0:
                raise ValueError("La cantidad no puede ser negativa.")

            execute("SET time_zone = '-03:00'")

            row = fetch_one("SELECT id FROM donaciones WHERE id=%s LIMIT 1", (int(request.id),))
            if not row:
                return pb.ApiResponse(success=False, message="Donación no encontrada.")

            # fecha_modificacion es TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
            # Forzamos NOW() (local) y usuario_modificacion
            sql = """
                UPDATE donaciones
                   SET descripcion=%s,
                       cantidad=%s,
                       fecha_modificacion=NOW(),
                       usuario_modificacion=%s
                 WHERE id=%s
            """
            rc, _ = execute(sql, (request.descripcion.strip(), int(request.cantidad), int(request.auth.actor_id), int(request.id)))
            ok = rc > 0
            return pb.ApiResponse(success=ok, message="Donación actualizada." if ok else "Sin cambios.")
        except (PermissionError, ValueError) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            print("[DONACIONES][Update][ERR]", e)
            return pb.ApiResponse(success=False, message="Error interno al actualizar.")

    # Baja lógica + auditoría
    def SoftDeleteDonationItem(self, request: pb.SoftDeleteDonationRequest, context):
        try:
            _require_vocal_o_presidente(request.auth)

            execute("SET time_zone = '-03:00'")

            row = fetch_one("SELECT id FROM donaciones WHERE id=%s LIMIT 1", (int(request.id),))
            if not row:
                return pb.ApiResponse(success=False, message="Donación no encontrada.")

            sql = """
                UPDATE donaciones
                   SET eliminado=1,
                       fecha_modificacion=NOW(),
                       usuario_modificacion=%s
                 WHERE id=%s
            """
            rc, _ = execute(sql, (int(request.auth.actor_id), int(request.id)))
            ok = rc > 0
            return pb.ApiResponse(success=ok, message="Donación eliminada lógicamente." if ok else "Sin cambios.")
        except (PermissionError,) as e:
            return pb.ApiResponse(success=False, message=str(e))
        except Exception as e:
            print("[DONACIONES][Delete][ERR]", e)
            return pb.ApiResponse(success=False, message="Error interno al eliminar.")

    # Listado
    def ListDonationItems(self, request: pb.Empty, context):
        try:
            execute("SET time_zone = '-03:00'")

            rows = fetch_all("""
                SELECT d.id, d.categoria_id, d.descripcion, d.cantidad, d.eliminado,
                       d.fecha_alta, d.usuario_alta, d.fecha_modificacion, d.usuario_modificacion
                  FROM donaciones d
                 ORDER BY d.eliminado ASC, d.categoria_id, d.descripcion
            """)
            items = [_row_to_pb(r) for r in (rows or [])]
            return pb.ListDonationsResponse(items=items)
        except Exception as e:
            print("[DONACIONES][List][ERR]", e)
            return pb.ListDonationsResponse(items=[])
