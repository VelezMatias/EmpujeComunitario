package com.empuje.graphql;

public class DonationFilter {
    private long id;
    private String nombre;
    private String categoria; // ALIMENTOS/ROPA/JUGUETES/UTILES_ESCOLARES o null
    private String from;      // "yyyy-MM-dd" o null
    private String to;        // "yyyy-MM-dd" o null
    private String eliminado; // "AMBOS" | "SI" | "NO" o null

    public DonationFilter() {}

    public DonationFilter(long id, String nombre, String categoria, String from, String to, String eliminado) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.from = from;
        this.to = to;
        this.eliminado = eliminado;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getEliminado() { return eliminado; }
    public void setEliminado(String eliminado) { this.eliminado = eliminado; }
}
