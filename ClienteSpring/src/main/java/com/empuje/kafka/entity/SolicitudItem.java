package com.empuje.kafka.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "solicitud_items",
       indexes = @Index(name = "idx_si_solicitud", columnList = "solicitud_id"))
public class SolicitudItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "solicitud_id", nullable = false, length = 100)
  private String solicitudId;

  @Column(name = "categoria", length = 50)
  private String categoria;

  @Column(name = "descripcion", length = 255)
  private String descripcion;

  @Column(name = "cantidad")
  private Integer cantidad;

  @Column(name = "unidad", length = 20)
  private String unidad;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
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
