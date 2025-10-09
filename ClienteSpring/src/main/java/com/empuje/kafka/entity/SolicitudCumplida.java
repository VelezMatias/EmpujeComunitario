package com.empuje.kafka.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_cumplidas")
public class SolicitudCumplida {
  @Id
  @Column(name = "solicitud_id", length = 100)
  private String solicitudId;

  @Column(name = "cumplida_at", insertable = false, updatable = false)
  private LocalDateTime cumplidaAt;

  public SolicitudCumplida() {}
  public SolicitudCumplida(String solicitudId) { this.solicitudId = solicitudId; }

  public String getSolicitudId() { return solicitudId; }
  public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
  public LocalDateTime getCumplidaAt() { return cumplidaAt; }
  public void setCumplidaAt(LocalDateTime cumplidaAt) { this.cumplidaAt = cumplidaAt; }
}
