package com.empuje.graphql;

public class VolunteerSavedFilter {
    private Long id;
    private String nombre;
    private String from;  // YYYY-MM-DD
    private String to;    // YYYY-MM-DD
    private String tipo;  // INTERNOS/EXTERNOS/AMBOS
    private Integer topN;
    private String createdAt;
    private String updatedAt;

    public VolunteerSavedFilter(Long id, String nombre, String from, String to, String tipo, Integer topN, String createdAt, String updatedAt) {
        this.id = id;
        this.nombre = nombre;
        this.from = from;
        this.to = to;
        this.tipo = tipo;
        this.topN = topN;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getTipo() { return tipo; }
    public Integer getTopN() { return topN; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
