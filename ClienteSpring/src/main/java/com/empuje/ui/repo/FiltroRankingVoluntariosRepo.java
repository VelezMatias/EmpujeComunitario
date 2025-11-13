package com.empuje.ui.repo;

import com.empuje.ui.entity.FiltroRankingVoluntarios;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FiltroRankingVoluntariosRepo extends JpaRepository<FiltroRankingVoluntarios, Long> {
    List<FiltroRankingVoluntarios> findByOwnerUserIdOrderByUpdatedAtDesc(Long ownerUserId);
    Optional<FiltroRankingVoluntarios> findByOwnerUserIdAndNombre(Long ownerUserId, String nombre);
    Optional<FiltroRankingVoluntarios> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
