package com.empuje.kafka.repo;

import com.empuje.kafka.entity.SolicitudExterna;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SolicitudExternaRepo extends CrudRepository<SolicitudExterna, Long> {
    Optional<SolicitudExterna> findBySolicitudId(String solicitudId);
}
