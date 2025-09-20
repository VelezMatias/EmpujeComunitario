package com.empuje.kafka.repo;

import com.empuje.kafka.entity.SolicitudItem;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SolicitudItemRepo extends CrudRepository<SolicitudItem, Long> {
    List<SolicitudItem> findBySolicitudId(String solicitudId);
}
