package com.empuje.ui.repo;

import com.empuje.ui.entity.FiltroDonaciones;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FiltroDonacionesRepo extends CrudRepository<FiltroDonaciones, Long> {
    List<FiltroDonaciones> findByOwnerUserIdOrderByNombreAsc(Long ownerUserId);

    Optional<FiltroDonaciones> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
