package com.empuje.kafka.repo;

import com.empuje.kafka.entity.SolicitudPropItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudPropItemRepository extends JpaRepository<SolicitudPropItem, Long> {
  List<SolicitudPropItem> findBySolicitudId(String solicitudId);
  long deleteBySolicitudId(String solicitudId);
}
