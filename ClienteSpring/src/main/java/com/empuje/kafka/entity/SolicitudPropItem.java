package com.empuje.kafka.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "solicitud_prop_items", indexes = {
  @Index(name = "idx_prop_items_sol", columnList = "solicitud_id")
})
public class SolicitudPropItem {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "solicitud_id", nullable = false)
  private String solicitudId;

  private String categoria;
  private String descripcion;
  private Integer cantidad;
  private String unidad;

  public Long getId() { return id; }
  public String getSolicitudId() { return solicitudId; }
  public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
  public String getCategoria() { return categoria; }
  public void setCategoria(String categoria) { this.categoria = categoria; }
  public String getDescripcion() { return descripcion; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public Integer getCantidad() { return cantidad; }
  public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
  public String getUnidad() { return unidad; }
  public void setUnidad(String unidad) { this.unidad = unidad; }
}
