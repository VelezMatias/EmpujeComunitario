package com.empuje.ui.repo;

import com.empuje.ui.entity.FiltroEventos;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository  // <- opcional, pero ayuda a la claridad
public interface FiltroEventosRepo extends CrudRepository<FiltroEventos, Long> {
  List<FiltroEventos> findByOwnerUserIdOrderByNombreAsc(Long ownerUserId);
  Optional<FiltroEventos> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
