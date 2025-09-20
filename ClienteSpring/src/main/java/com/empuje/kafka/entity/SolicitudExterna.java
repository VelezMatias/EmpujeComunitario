package com.empuje.kafka.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_externas",
       indexes = {
         @Index(name = "idx_se_solicitud", columnList = "solicitud_id", unique = true),
         @Index(name = "idx_se_estado", columnList = "estado")
       })
public class SolicitudExterna {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "org_id", nullable = false)
  private Integer orgId;

  @Column(name = "solicitud_id", nullable = false, length = 100, unique = true)
  private String solicitudId;

  @Column(name = "fecha_hora")
  private LocalDateTime fechaHora;

  @Column(name = "estado", length = 20)
  private String estado;

  @Lob
  @Column(name = "payload_json")
  private String payloadJson;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Integer getOrgId() { return orgId; }
  public void setOrgId(Integer orgId) { this.orgId = orgId; }
  public String getSolicitudId() { return solicitudId; }
  public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
  public LocalDateTime getFechaHora() { return fechaHora; }
  public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
  public String getEstado() { return estado; }
  public void setEstado(String estado) { this.estado = estado; }
  public String getPayloadJson() { return payloadJson; }
  public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}
