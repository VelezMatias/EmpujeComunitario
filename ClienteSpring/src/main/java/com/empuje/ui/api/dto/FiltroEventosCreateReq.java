package com.empuje.ui.api.dto;

import com.empuje.ui.entity.FiltroEventos.Reparto;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record FiltroEventosCreateReq(
    @JsonProperty("nombre")
    String nombre,

    // Acepta "usuarioObjetivoId" (el que manda tu JS) y también "usuarioId"/"usuario"
    @JsonProperty("usuarioObjetivoId")
    @JsonAlias({"usuarioId", "usuario"})
    Long usuarioObjetivoId,

    @JsonProperty("fechaDesde")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaDesde,

    @JsonProperty("fechaHasta")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaHasta,

    @JsonProperty("reparto")
    Reparto reparto
) {}
