package com.empuje.kafka.repo;

import com.empuje.kafka.entity.EventoExterno;
import org.springframework.data.repository.CrudRepository;

public interface EventoExternoRepo extends CrudRepository<EventoExterno, Long> {
}
