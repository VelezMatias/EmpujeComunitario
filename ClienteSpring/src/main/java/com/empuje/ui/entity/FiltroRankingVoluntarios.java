package com.empuje.ui.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "filtros_ranking_voluntarios",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_owner_nombre", columnNames = {"owner_user_id","nombre"})
    }
)
public class FiltroRankingVoluntarios {

    public enum TipoEvento {
        INTERNOS, EXTERNOS, AMBOS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="owner_user_id", nullable=false)
    private Long ownerUserId;

    @Column(name="nombre", nullable=false, length=120)
    private String nombre;

    @Column(name="fecha_desde", nullable=false)
    private LocalDate fechaDesde;

    @Column(name="fecha_hasta", nullable=false)
    private LocalDate fechaHasta;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo_evento", nullable=false, length=16)
    private TipoEvento tipoEvento;

    @Column(name="top_n", nullable=false)
    private Integer topN;

    // Estos campos los mantiene MySQL; los mapeo como solo-lectura
    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;

    // Getters/Setters
    public Long getId() { return id; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }
    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }
    public TipoEvento getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoEvento tipoEvento) { this.tipoEvento = tipoEvento; }
    public Integer getTopN() { return topN; }
    public void setTopN(Integer topN) { this.topN = topN; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
