package com.empuje.kafka.repo;

import com.empuje.kafka.entity.SolicitudItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudItemRepo extends JpaRepository<SolicitudItem, Long> {
    List<SolicitudItem> findBySolicitudId(String solicitudId);


    long deleteBySolicitudId(String solicitudId);
}
