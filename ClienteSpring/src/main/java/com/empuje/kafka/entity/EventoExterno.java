package com.empuje.kafka.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_externos", indexes = {
        @Index(name = "idx_eventos_org", columnList = "org_id"),
        @Index(name = "idx_eventos_evento", columnList = "evento_id")
})
public class EventoExterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Column(name = "evento_id", nullable = false, length = 100)
    private String eventoId;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "payload_json", columnDefinition = "json")
    private String payloadJson;

    @Column(name = "titulo", length = 200)
    private String titulo;

    @Column(name = "lugar", length = 200)
    private String lugar;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public EventoExterno() {
    }

    public EventoExterno(Integer orgId, String eventoId, LocalDateTime fechaHora,
            String estado, String payloadJson, String titulo, String lugar) {
        this.orgId = orgId;
        this.eventoId = eventoId;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.payloadJson = payloadJson;
        this.titulo = titulo;
        this.lugar = lugar;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
