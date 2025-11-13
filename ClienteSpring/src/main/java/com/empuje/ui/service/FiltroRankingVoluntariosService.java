package com.empuje.ui.service;

import com.empuje.ui.entity.FiltroRankingVoluntarios;
import com.empuje.ui.entity.FiltroRankingVoluntarios.TipoEvento;
import com.empuje.ui.repo.FiltroRankingVoluntariosRepo;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FiltroRankingVoluntariosService {

    private final FiltroRankingVoluntariosRepo repo;

    public FiltroRankingVoluntariosService(FiltroRankingVoluntariosRepo repo) {
        this.repo = repo;
    }

    public List<FiltroRankingVoluntarios> listar(Long ownerId) {
        return repo.findByOwnerUserIdOrderByUpdatedAtDesc(ownerId);
    }

    @Transactional
    public FiltroRankingVoluntarios crear(Long ownerId, String nombre, LocalDate desde, LocalDate hasta, TipoEvento tipo, Integer topN) {
        validarRangoFechas(desde, hasta);
        validarTopN(topN);

        FiltroRankingVoluntarios f = new FiltroRankingVoluntarios();
        f.setOwnerUserId(ownerId);
        f.setNombre(nombre.trim());
        f.setFechaDesde(desde);
        f.setFechaHasta(hasta);
        f.setTipoEvento(tipo);
        f.setTopN(topN);

        try {
            return repo.save(f);
        } catch (DataIntegrityViolationException e) {
            // Probable violación de uq_owner_nombre
            throw new IllegalArgumentException("Ya existe un filtro con ese nombre para este usuario.", e);
        }
    }

    @Transactional
    public FiltroRankingVoluntarios actualizar(Long ownerId, Long id, String nombre, LocalDate desde, LocalDate hasta, TipoEvento tipo, Integer topN) {
        FiltroRankingVoluntarios f = repo.findByIdAndOwnerUserId(id, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el filtro o no sos el dueño."));

        validarRangoFechas(desde, hasta);
        validarTopN(topN);

        f.setNombre(nombre.trim());
        f.setFechaDesde(desde);
        f.setFechaHasta(hasta);
        f.setTipoEvento(tipo);
        f.setTopN(topN);

        try {
            return repo.save(f);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ya existe un filtro con ese nombre para este usuario.", e);
        }
    }

    @Transactional
    public void borrar(Long ownerId, Long id) {
        FiltroRankingVoluntarios f = repo.findByIdAndOwnerUserId(id, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el filtro o no sos el dueño."));
        repo.delete(f);
    }

    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Rango de fechas obligatorio (desde/hasta).");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha hasta no puede ser anterior a la fecha desde.");
        }
    }

    private void validarTopN(Integer topN) {
        if (topN == null || topN < 3) {
            throw new IllegalArgumentException("topN debe ser al menos 3.");
        }
    }
}
