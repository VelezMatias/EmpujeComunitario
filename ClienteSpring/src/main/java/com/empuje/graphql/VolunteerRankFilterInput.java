package com.empuje.graphql;

public class VolunteerRankFilterInput {
    private String from;          // YYYY-MM-DD (puede venir vacío -> default en resolver)
    private String to;            // YYYY-MM-DD (puede venir vacío -> default en resolver)
    private EventoTipo tipo;      // INTERNOS | EXTERNOS | AMBOS
    private Integer topN;         // mínimo 3

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public EventoTipo getTipo() { return tipo; }
    public void setTipo(EventoTipo tipo) { this.tipo = tipo; }
    public Integer getTopN() { return topN; }
    public void setTopN(Integer topN) { this.topN = topN; }
}
