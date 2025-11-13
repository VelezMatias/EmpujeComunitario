package com.empuje.graphql;

import com.empuje.ui.entity.FiltroRankingVoluntarios;
import com.empuje.ui.entity.FiltroRankingVoluntarios.TipoEvento;

public class VolunteerSavedFilterInput {
    private String nombre;
    private String from;   // YYYY-MM-DD
    private String to;     // YYYY-MM-DD
    private TipoEvento tipo; //enum
    private Integer topN;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }

    public Integer getTopN() { return topN; }
    public void setTopN(Integer topN) { this.topN = topN; }
}
