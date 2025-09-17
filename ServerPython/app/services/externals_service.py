# app/services/externals_service.py
import os
import pymysql
from dotenv import load_dotenv

from app import ong_pb2, ong_pb2_grpc

load_dotenv()

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "3306"))
DB_NAME = os.getenv("DB_NAME", "empujecomunitario")
DB_USER = os.getenv("DB_USER", "empuje")
DB_PASS = os.getenv("DB_PASS", "empuje123")


def _get_conn():
    return pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASS,
        database=DB_NAME,
        autocommit=False,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


class ExternalsService(ong_pb2_grpc.OngServiceServicer):
    """
    Implementa:
      - ListSolicitudesExternas(ListExternasRequest) -> ListSolicitudesExternasResponse
      - ListEventosExternos(ListExternasRequest) -> ListEventosExternosResponse
    Asegurate de haber regenerado stubs (ong_pb2*.py) con los nuevos mensajes y RPCs en ong.proto.
    """

    def ListSolicitudesExternas(self, request, context):
        page = request.page or 1
        size = request.page_size or 20
        off = (page - 1) * size

        sql = """
        SELECT org_id, solicitud_id, estado,
               DATE_FORMAT(fecha_hora, '%%Y-%%m-%%d %%H:%%i:%%s') AS fecha_hora
        FROM solicitudes_externas
        ORDER BY id DESC
        LIMIT %s OFFSET %s
        """

        with _get_conn() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, (size + 1, off))
                rows = cur.fetchall()

        has_more = len(rows) > size
        data = []
        for r in rows[:size]:
            data.append(
                ong_pb2.SolicitudExterna(
                    org_id=r.get("org_id") or 0,
                    solicitud_id=r.get("solicitud_id") or "",
                    estado=r.get("estado") or "VIGENTE",
                    fecha_hora=r.get("fecha_hora") or "",
                )
            )

        return ong_pb2.ListSolicitudesExternasResponse(
            data=data, page=page, page_size=size, has_more=has_more
        )

    def ListEventosExternos(self, request, context):
        page = request.page or 1
        size = request.page_size or 20
        off = (page - 1) * size

        sql = """
        SELECT org_id, evento_id, estado,
               DATE_FORMAT(fecha_hora, '%%Y-%%m-%%d %%H:%%i:%%s') AS fecha_hora,
               JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.titulo')) AS titulo,
               JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.lugar')) AS lugar
        FROM eventos_externos
        ORDER BY id DESC
        LIMIT %s OFFSET %s
        """

        with _get_conn() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, (size + 1, off))
                rows = cur.fetchall()

        has_more = len(rows) > size
        data = []
        for r in rows[:size]:
            data.append(
                ong_pb2.EventExterno(
                    org_id=r.get("org_id") or 0,
                    evento_id=r.get("evento_id") or "",
                    estado=r.get("estado") or "VIGENTE",
                    fecha_hora=r.get("fecha_hora") or "",
                    titulo=(r.get("titulo") or ""),
                    lugar=(r.get("lugar") or ""),
                )
            )

        return ong_pb2.ListEventosExternosResponse(
            data=data, page=page, page_size=size, has_more=has_more
        )
