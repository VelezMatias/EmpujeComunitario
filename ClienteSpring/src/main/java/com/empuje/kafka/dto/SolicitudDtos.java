package com.empuje.kafka.dto;

import java.util.List;


public class SolicitudDtos {

    public static class SolicitudDonaciones {
        private Integer org_id; // opcional (lo dejamos por compatibilidad, no lo mostramos)
        private String solicitud_id; // autogenerado si falta
        private String fecha_hora; // autogenerado si falta
        private String idempotency_key; // autogenerado si falta
        private List<Item> items;

        public Integer getOrg_id() {
            return org_id;
        }

        public void setOrg_id(Integer org_id) {
            this.org_id = org_id;
        }

        public String getSolicitud_id() {
            return solicitud_id;
        }

        public void setSolicitud_id(String solicitud_id) {
            this.solicitud_id = solicitud_id;
        }

        public String getFecha_hora() {
            return fecha_hora;
        }

        public void setFecha_hora(String fecha_hora) {
            this.fecha_hora = fecha_hora;
        }

        public String getIdempotency_key() {
            return idempotency_key;
        }

        public void setIdempotency_key(String idempotency_key) {
            this.idempotency_key = idempotency_key;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    public static class Item {
        private String categoria; // ALIMENTOS/ROPA/JUGUETES/UTILES_ESCOLARES
        private String descripcion;
        private Integer cantidad;
        private String unidad; // u/kg/l

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public String getUnidad() {
            return unidad;
        }

        public void setUnidad(String unidad) {
            this.unidad = unidad;
        }
    }
}
