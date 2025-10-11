package com.empuje.kafka.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_externas", indexes = {
    @Index(name = "idx_solext_org", columnList = "org_id"),
    @Index(name = "idx_solext_solicitud", columnList = "solicitud_id", unique = true)
})
public class SolicitudExterna {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ⬇ org_id es OPCIONAL → nullable = true
  @Column(name = "org_id", nullable = true)
  private Integer orgId;

  @Column(name = "solicitud_id", nullable = false, length = 100, unique = true)
  private String solicitudId;

  @Column(name = "fecha_hora")
  private LocalDateTime fechaHora;

  @Column(name = "estado", length = 20)
  private String estado;

  @Column(name = "payload_json", columnDefinition = "json")
  private String payloadJson;

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt;

  public SolicitudExterna() {
  }

  public SolicitudExterna(Integer orgId, String solicitudId, LocalDateTime fechaHora,
      String estado, String payloadJson) {
    this.orgId = orgId;
    this.solicitudId = solicitudId;
    this.fechaHora = fechaHora;
    this.estado = estado;
    this.payloadJson = payloadJson;
  }

  public Long getId() {
    return id;
  }

  public Integer getOrgId() {
    return orgId;
  }

  public void setOrgId(Integer orgId) {
    this.orgId = orgId;
  }

  public String getSolicitudId() {
    return solicitudId;
  }

  public void setSolicitudId(String solicitudId) {
    this.solicitudId = solicitudId;
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
