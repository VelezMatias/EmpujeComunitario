package com.empuje.kafka.repo;

import com.empuje.kafka.entity.SolicitudCumplida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudCumplidaRepo extends JpaRepository<SolicitudCumplida, String> {
    boolean existsBySolicitudId(String solicitudId);
}