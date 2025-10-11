package com.empuje.kafka.repo;

import com.empuje.kafka.entity.MensajeProcesado;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MensajeProcesadoRepo extends CrudRepository<MensajeProcesado, Long> {
    Optional<MensajeProcesado> findByTopicAndPartitionNoAndOffsetNo(String topic, Integer partitionNo, Long offsetNo);
}
