package com.empuje.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class Payloads {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String categoria;
        private String descripcion;
        private Integer cantidad;
        private String unidad;

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

    // -------- Solicitudes --------
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SolicitudDonaciones {
        private Integer org_id;
        private String solicitud_id;
        private List<Item> items;
        private String fecha_hora;
        private String idempotency_key;

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

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
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
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BajaSolicitudDonaciones {
        private Integer org_id;
        private String solicitud_id;
        private String fecha_hora;
        private String idempotency_key;

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
    }

    // -------- Eventos --------
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventoSolidario {
        private Integer org_id;
        private String evento_id;
        private String titulo;
        private String fecha_hora;
        private String lugar;
        private String idempotency_key;

        public Integer getOrg_id() {
            return org_id;
        }

        public void setOrg_id(Integer org_id) {
            this.org_id = org_id;
        }

        public String getEvento_id() {
            return evento_id;
        }

        public void setEvento_id(String evento_id) {
            this.evento_id = evento_id;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getFecha_hora() {
            return fecha_hora;
        }

        public void setFecha_hora(String fecha_hora) {
            this.fecha_hora = fecha_hora;
        }

        public String getLugar() {
            return lugar;
        }

        public void setLugar(String lugar) {
            this.lugar = lugar;
        }

        public String getIdempotency_key() {
            return idempotency_key;
        }

        public void setIdempotency_key(String idempotency_key) {
            this.idempotency_key = idempotency_key;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BajaEventoSolidario {
        private Integer org_id;
        private String evento_id;
        private String fecha_hora;
        private String idempotency_key;

        public Integer getOrg_id() {
            return org_id;
        }

        public void setOrg_id(Integer org_id) {
            this.org_id = org_id;
        }

        public String getEvento_id() {
            return evento_id;
        }

        public void setEvento_id(String evento_id) {
            this.evento_id = evento_id;
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
    }
}
