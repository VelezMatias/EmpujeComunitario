package com.empuje.ui.api.dto;

import com.empuje.ui.entity.FiltroEventos.Reparto;
import java.time.LocalDate;

public record FiltroEventosRes(
    Long id,
    String nombre,
    Long usuarioObjetivoId,
    LocalDate fechaDesde,
    LocalDate fechaHasta,
    Reparto reparto
) {}
