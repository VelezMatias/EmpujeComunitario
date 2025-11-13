package com.empuje.graphql;

import com.empuje.ui.entity.FiltroRankingVoluntarios;
import com.empuje.ui.entity.FiltroRankingVoluntarios.TipoEvento;
import com.empuje.ui.service.FiltroRankingVoluntariosService;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.Argument;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class VolunteerFiltersResolver {

    private final FiltroRankingVoluntariosService service;
    private final HttpSession session;

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public VolunteerFiltersResolver(FiltroRankingVoluntariosService service, HttpSession session) {
        this.service = service;
        this.session = session;
    }

    // ===== Helpers =====
    private Long currentUserId() {
        Object v = session.getAttribute("userId");
        if (v == null) throw new IllegalStateException("Sesión inválida: userId no seteado.");
        return (v instanceof Number) ? ((Number) v).longValue() : Long.parseLong(v.toString());
    }

    private static VolunteerSavedFilter toDto(FiltroRankingVoluntarios f) {
        return new VolunteerSavedFilter(
            f.getId(),
            f.getNombre(),
            f.getFechaDesde().format(F),
            f.getFechaHasta().format(F),
            f.getTipoEvento().name(),
            f.getTopN(),
            f.getCreatedAt() != null ? f.getCreatedAt().toString() : null,
            f.getUpdatedAt() != null ? f.getUpdatedAt().toString() : null
        );
    }

    private static LocalDate parseOrDefault(String v, LocalDate def) {
        if (v == null || v.isBlank()) return def;
        return LocalDate.parse(v, F);
    }

    // ===== Queries =====
    @QueryMapping(name = "volunteerSavedFilters")
    public List<VolunteerSavedFilter> volunteerSavedFilters() {
        Long ownerId = currentUserId();
        return service.listar(ownerId).stream().map(VolunteerFiltersResolver::toDto).toList();
    }

    // ===== Mutations =====
    @MutationMapping(name = "saveVolunteerFilter")
    public VolunteerSavedFilter saveVolunteerFilter(@Argument("input") VolunteerSavedFilterInput input) {
        Long ownerId = currentUserId();

        LocalDate desde = parseOrDefault(input.getFrom(), LocalDate.of(2000,1,1));
        LocalDate hasta = parseOrDefault(input.getTo(), LocalDate.now());
        TipoEvento tipo = input.getTipo() != null ? input.getTipo() : TipoEvento.INTERNOS;
        Integer topN = (input.getTopN() == null || input.getTopN() < 3) ? 3 : input.getTopN();

        FiltroRankingVoluntarios saved = service.crear(ownerId, input.getNombre(), desde, hasta, tipo, topN);
        return toDto(saved);
    }

    @MutationMapping(name = "updateVolunteerFilter")
    public VolunteerSavedFilter updateVolunteerFilter(@Argument Long id, @Argument("input") VolunteerSavedFilterInput input) {
        Long ownerId = currentUserId();

        LocalDate desde = parseOrDefault(input.getFrom(), LocalDate.of(2000,1,1));
        LocalDate hasta = parseOrDefault(input.getTo(), LocalDate.now());
        TipoEvento tipo = input.getTipo() != null ? input.getTipo() : TipoEvento.INTERNOS;
        Integer topN = (input.getTopN() == null || input.getTopN() < 3) ? 3 : input.getTopN();

        FiltroRankingVoluntarios updated = service.actualizar(ownerId, id, input.getNombre(), desde, hasta, tipo, topN);
        return toDto(updated);
    }

    @MutationMapping(name = "deleteVolunteerFilter")
    public Boolean deleteVolunteerFilter(@Argument Long id) {
        Long ownerId = currentUserId();
        service.borrar(ownerId, id);
        return Boolean.TRUE;
    }
}
