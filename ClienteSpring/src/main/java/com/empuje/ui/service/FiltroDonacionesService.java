package com.empuje.ui.service;

import com.empuje.graphql.DonationFilter;
import com.empuje.ui.entity.FiltroDonaciones;
import com.empuje.ui.repo.FiltroDonacionesRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/*
  Servicio de filtros de Donaciones (persistencia).
  - Resuelve el owner desde HttpSession (atributo "userId").
  - Mapea entidad JPA <-> DTO GraphQL (DonationFilter).
  - Expuesto por el Resolver GraphQL (DonationFilterResolver).
 */
@Service
public class FiltroDonacionesService {

    private final FiltroDonacionesRepo repo;
    private final HttpSession session;

    public FiltroDonacionesService(FiltroDonacionesRepo repo, HttpSession session) {
        this.repo = repo;
        this.session = session;
    }

    /* ===================== Helpers ===================== */

    /* Obtiene el ID del usuario logueado desde la sesión. */
    private Long ownerId() {
        Object v = (session != null) ? session.getAttribute("userId") : null;
        if (v instanceof Number n)
            return n.longValue();
        if (v instanceof String s && !s.isBlank())
            return Long.valueOf(s);
        throw new IllegalStateException("No hay 'userId' en sesión");
    }

    private static String toStr(LocalDate d) {
        return d == null ? null : d.toString();
    }

    private static LocalDate toDate(String s) {
        return (s == null || s.isBlank()) ? null : LocalDate.parse(s);
    }

    /* Convierte la entidad persistida al DTO GraphQL esperado por el front. */
    private DonationFilter toDto(FiltroDonaciones e) {
        DonationFilter dto = new DonationFilter();
        dto.setId(e.getId());
        dto.setNombre(e.getNombre());
        dto.setCategoria(e.getCategoria());
        dto.setFrom(toStr(e.getFechaDesde()));
        dto.setTo(toStr(e.getFechaHasta()));
        dto.setEliminado(e.getEliminado() == null ? null : e.getEliminado().name());
        return dto;
    }

    /* ===================== Operaciones ===================== */

    @Transactional(readOnly = true)
    public List<DonationFilter> listMine() {
        Long owner = ownerId();
        return repo.findByOwnerUserIdOrderByNombreAsc(owner)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DonationFilter create(String nombre, String categoria, String from, String to, String eliminado) {
        Long owner = ownerId();

        String nombreOk = Objects.requireNonNull(nombre, "nombre requerido").trim();
        if (nombreOk.isEmpty())
            throw new IllegalArgumentException("nombre no puede estar vacío");

        FiltroDonaciones e = new FiltroDonaciones();
        e.setOwnerUserId(owner);
        e.setNombre(nombreOk);
        e.setCategoria(categoria == null || categoria.isBlank() ? null : categoria.trim());
        e.setFechaDesde(toDate(from));
        e.setFechaHasta(toDate(to));

        if (eliminado != null && !eliminado.isBlank()) {
            e.setEliminado(FiltroDonaciones.Eliminado.valueOf(eliminado.toUpperCase()));
        } else {
            e.setEliminado(null); // NULL = “no filtrar por eliminado” / equivalente a AMBOS
        }

        repo.save(e);
        return toDto(e);
    }

    @Transactional
    public DonationFilter update(Long id, String nombre, String categoria, String from, String to, String eliminado) {
        Long owner = ownerId();
        FiltroDonaciones e = repo.findByIdAndOwnerUserId(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("No existe el filtro o no es dueño"));

        if (nombre != null) {
            String n = nombre.trim();
            if (n.isEmpty())
                throw new IllegalArgumentException("nombre no puede estar vacío");
            e.setNombre(n);
        }

        e.setCategoria(categoria == null || categoria.isBlank() ? null : categoria.trim());
        e.setFechaDesde(toDate(from));
        e.setFechaHasta(toDate(to));

        if (eliminado == null || eliminado.isBlank()) {
            e.setEliminado(null);
        } else {
            e.setEliminado(FiltroDonaciones.Eliminado.valueOf(eliminado.toUpperCase()));
        }

        repo.save(e);
        return toDto(e);
    }

    @Transactional
    public boolean delete(Long id) {
        Long owner = ownerId();
        FiltroDonaciones e = repo.findByIdAndOwnerUserId(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("No existe el filtro o no es dueño"));
        repo.delete(e);
        return true;
    }
}
