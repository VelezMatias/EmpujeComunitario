package com.empuje.kafka;

import java.util.List;

// Tipos simples para mapear a JSON (ObjectMapper usa snake_case)
public final class Payloads {
    private Payloads() {}

    public static class Item {
        public String categoria;
        public String descripcion;
        public Integer cantidad; // puede ser null si no aplica
        public String unidad;    // puede ser null si no aplica

        public Item() {}
        public Item(String categoria, String descripcion, Integer cantidad, String unidad) {
            this.categoria = categoria;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.unidad = unidad;
        }
    }

    public static class SolicitudDonaciones {
        public Integer orgId;              // si es null, lo rellenamos con app.org-id
        public String solicitudId;
        public List<Item> items;
        public String fechaHora;          // ISO-8601 con offset
        public String idempotencyKey;     // si es null, lo rellenamos: "<orgId>:<solicitudId>"
    }

    public static class BajaSolicitudDonaciones {
        public Integer orgId;
        public String solicitudId;
        public String motivo;
        public String fechaHora;
        public String idempotencyKey; // "<orgId>:<solicitudId>:BAJA"
    }

    public static class OfertaDonaciones {
        public Integer orgId;
        public String ofertaId;
        public List<Item> items;
        public String fechaHora;
        public String idempotencyKey;
    }

    public static class EventoSolidario {
        public Integer orgId;
        public String eventoId;
        public String titulo;
        public String descripcion;
        public String lugar;
        public String fechaInicio;
        public String fechaFin;
        public String idempotencyKey;
    }

    public static class BajaEventoSolidario {
        public Integer orgId;
        public String eventoId;
        public String motivo;
        public String fechaHora;
        public String idempotencyKey; // "<orgId>:<eventoId>:BAJA"
    }

    public static class AdhesionEvento {
        public Integer orgIdOrganizador;  // para construir topic adhesion-evento.<orgIdOrganizador>
        public String  eventoId;
        public Integer orgIdAdherente;    // tu organización que se adhiere (normalmente app.org-id)
        public String  fechaHora;
        public String  idempotencyKey;    // "<orgIdAdherente>:<eventoId>:ADH"
    }
}
