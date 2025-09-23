package com.empuje.kafka.repo;

import com.empuje.kafka.entity.SolicitudExterna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SolicitudExternaRepo extends JpaRepository<SolicitudExterna, Long> {
    Optional<SolicitudExterna> findBySolicitudId(String solicitudId);

    // Listado más reciente primero
    Page<SolicitudExterna> findAllByOrderByIdDesc(Pageable pageable);
}