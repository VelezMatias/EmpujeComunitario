package com.empuje.kafka.service;
import com.empuje.kafka.config.OrgConfig;

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
    private final OrgConfig org; // <- NUEVO

    public ExternoConsumerService(ObjectMapper om,
                                  SolicitudExternaRepo solicitudes,
                                  SolicitudItemRepo items,
                                  MensajeProcesadoRepo procesados,
                                  OrgConfig org) {
        this.om = om;
        this.solicitudes = solicitudes;
        this.items = items;
        this.procesados = procesados;
        this.org = org;
    }

    @KafkaListener(topics = "solicitud-donaciones", groupId = "cliente-spring-fix")
    @Transactional
    public void onSolicitud(ConsumerRecord<String, String> record) throws Exception {
        log.info("Kafka RX topic={} key={} partition={} offset={}",
                record.topic(), record.key(), record.partition(), record.offset());

        if (procesados.findByTopicAndPartitionNoAndOffsetNo(
                record.topic(), record.partition(), record.offset()).isPresent()) {
            log.info("Mensaje ya procesado (idempotencia) offset={}", record.offset());
            return;
        }

        // Ignoro mis propias solicitudes si por algún motivo me llegan
        if (record.key() != null && record.key().equals(String.valueOf(org.getOrgId()))) {
            log.info("Descarto solicitud propia (key==ORG_ID={})", org.getOrgId());
            marcarProcesado(record);
            return;
        }

        var dto = om.readValue(record.value(), SolicitudDtos.SolicitudDonaciones.class);
        log.debug("Payload DTO: solicitud_id={} items={}",
                dto.getSolicitud_id(), dto.getItems() == null ? 0 : dto.getItems().size());

        final String solicitudId = dto.getSolicitud_id();

        // org_id del JSON o lo infiero desde la key del record
        Integer orgId = dto.getOrg_id();
        if (orgId == null && record.key() != null) {
            try { orgId = Integer.parseInt(record.key()); } catch (NumberFormatException ignored) {}
        }

        LocalDateTime fecha = (dto.getFecha_hora() != null && !dto.getFecha_hora().isBlank())
                ? OffsetDateTime.parse(dto.getFecha_hora(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime()
                : LocalDateTime.now();

        var se = solicitudes.findBySolicitudId(solicitudId).orElse(new SolicitudExterna());
        se.setSolicitudId(solicitudId);
        se.setOrgId(orgId);
        se.setFechaHora(fecha);
        se.setEstado("VIGENTE");
        se.setPayloadJson(record.value());
        solicitudes.save(se);

        long borrados = items.deleteBySolicitudId(solicitudId);
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

        marcarProcesado(record);
    }

    private void marcarProcesado(ConsumerRecord<String,String> record) {
        var mp = new MensajeProcesado();
        mp.setTopic(record.topic());
        mp.setMessageKey(record.key());
        mp.setPartitionNo(record.partition());
        mp.setOffsetNo(record.offset());
        procesados.save(mp);
    }
}