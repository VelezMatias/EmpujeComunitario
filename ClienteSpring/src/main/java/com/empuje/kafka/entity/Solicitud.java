
package com.empuje.kafka.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes", indexes = {
  @Index(name = "idx_sol_org", columnList = "org_id"),
  @Index(name = "idx_sol_codigo", columnList = "solicitud_id", unique = true)
})
public class Solicitud {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "org_id", nullable = false)
  private Integer orgId;

  @Column(name = "solicitud_id", nullable = false, length = 100, unique = true)
  private String solicitudId;

  @Column(name = "fecha_hora", nullable = false)
  private LocalDateTime fechaHora;

  @Column(name = "payload_json", columnDefinition = "json", nullable = false)
  private String payloadJson;

  // getters/setters
  public Long getId() { return id; }
  public Integer getOrgId() { return orgId; }
  public void setOrgId(Integer orgId) { this.orgId = orgId; }
  public String getSolicitudId() { return solicitudId; }
  public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
  public LocalDateTime getFechaHora() { return fechaHora; }
  public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
  public String getPayloadJson() { return payloadJson; }
  public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}

