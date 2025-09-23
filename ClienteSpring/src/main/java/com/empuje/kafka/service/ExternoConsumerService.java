package com.empuje.kafka.service;

import com.empuje.kafka.dto.SolicitudDtos;
import com.empuje.kafka.entity.MensajeProcesado;
import com.empuje.kafka.entity.SolicitudExterna;
import com.empuje.kafka.entity.SolicitudItem;
import com.empuje.kafka.repo.MensajeProcesadoRepo;
import com.empuje.kafka.repo.SolicitudExternaRepo;
import com.empuje.kafka.repo.SolicitudItemRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ExternoConsumerService {

    private static final Logger log = LoggerFactory.getLogger(ExternoConsumerService.class);

    private final ObjectMapper om;
    private final SolicitudExternaRepo solicitudes;
    private final SolicitudItemRepo items;
    private final MensajeProcesadoRepo procesados;

    public ExternoConsumerService(ObjectMapper om,
            SolicitudExternaRepo solicitudes,
            SolicitudItemRepo items,
            MensajeProcesadoRepo procesados) {
        this.om = om;
        this.solicitudes = solicitudes;
        this.items = items;
        this.procesados = procesados;
    }

    @KafkaListener(topics = "solicitud-donaciones", groupId = "cliente-spring-fix")
    @Transactional
    public void onSolicitud(ConsumerRecord<String, String> record) throws Exception {
        log.info("Kafka RX topic={} key={} partition={} offset={}",
                record.topic(), record.key(), record.partition(), record.offset());

        // Idempotencia por (topic, partition, offset)
        if (procesados.findByTopicAndPartitionNoAndOffsetNo(
                record.topic(), record.partition(), record.offset()).isPresent()) {
            log.info("Mensaje ya procesado (idempotencia) offset={}", record.offset());
            return;
        }

        var dto = om.readValue(record.value(), SolicitudDtos.SolicitudDonaciones.class);
        log.debug("Payload DTO: solicitud_id={} items={}",
                dto.getSolicitud_id(), dto.getItems() == null ? 0 : dto.getItems().size());

        final String solicitudId = dto.getSolicitud_id();
        final Integer orgId = dto.getOrg_id(); // puede ser null

        LocalDateTime fecha = (dto.getFecha_hora() != null && !dto.getFecha_hora().isBlank())
                ? OffsetDateTime.parse(dto.getFecha_hora(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime()
                : null;

        if (fecha == null) {
            fecha = LocalDateTime.now();
        }

        var se = solicitudes.findBySolicitudId(solicitudId).orElse(new SolicitudExterna());
        se.setSolicitudId(solicitudId);
        se.setOrgId(orgId);
        se.setFechaHora(fecha);
        se.setEstado("VIGENTE");
        se.setPayloadJson(record.value());
        solicitudes.save(se);
        log.info("UPSERT solicitudes_externas OK solicitud_id={}", solicitudId);

        // Borrado atómico por solicitud_id
        long borrados = items.deleteBySolicitudId(solicitudId);
        log.debug("Items borrados previos: {}", borrados);

        if (dto.getItems() != null) {
            for (var it : dto.getItems()) {
                var row = new SolicitudItem();
                row.setSolicitudId(solicitudId);
                row.setCategoria(it.getCategoria());
                row.setDescripcion(it.getDescripcion());
                row.setCantidad(it.getCantidad());
                row.setUnidad(it.getUnidad());
                items.save(row);
            }
        }
        log.info("Insert items OK solicitud_id={} count={}",
                solicitudId, dto.getItems() == null ? 0 : dto.getItems().size());

        var mp = new MensajeProcesado();
        mp.setTopic(record.topic());
        mp.setMessageKey(record.key());
        mp.setPartitionNo(record.partition());
        mp.setOffsetNo(record.offset());
        procesados.save(mp);
        log.info("Marca mensajes_procesados OK offset={}", record.offset());
    }
}
