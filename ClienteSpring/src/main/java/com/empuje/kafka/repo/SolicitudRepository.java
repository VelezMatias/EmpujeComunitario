package com.empuje.kafka.repo;

import com.empuje.kafka.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
  Optional<Solicitud> findBySolicitudId(String solicitudId);
}
