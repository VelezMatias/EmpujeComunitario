package com.empuje.ui.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "filtros_eventos",
       uniqueConstraints = @UniqueConstraint(name="uq_owner_nombre",
                     columnNames = {"owner_user_id","nombre"}))
public class FiltroEventos {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="owner_user_id", nullable=false)
  private Long ownerUserId;

  @Column(nullable=false, length=120)
  private String nombre;

  @Column(name="usuario_objetivo_id", nullable=false)
  private Long usuarioObjetivoId;

  @Column(name="fecha_desde")
  private LocalDate fechaDesde;

  @Column(name="fecha_hasta")
  private LocalDate fechaHasta;

  @Enumerated(EnumType.STRING)
  @Column(name="reparto")
  private Reparto reparto; // SI, NO, AMBOS

  @Column(name="created_at", insertable=false, updatable=false)
  private LocalDateTime createdAt;

  @Column(name="updated_at", insertable=false, updatable=false)
  private LocalDateTime updatedAt;

  public enum Reparto { SI, NO, AMBOS }

  // getters/setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getOwnerUserId() { return ownerUserId; }
  public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public Long getUsuarioObjetivoId() { return usuarioObjetivoId; }
  public void setUsuarioObjetivoId(Long usuarioObjetivoId) { this.usuarioObjetivoId = usuarioObjetivoId; }
  public LocalDate getFechaDesde() { return fechaDesde; }
  public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }
  public LocalDate getFechaHasta() { return fechaHasta; }
  public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }
  public Reparto getReparto() { return reparto; }
  public void setReparto(Reparto reparto) { this.reparto = reparto; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
}
