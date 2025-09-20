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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ExternoConsumerService {

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

    @KafkaListener(topics = "solicitud-donaciones", groupId = "cliente-spring")
    @Transactional
    public void onSolicitud(ConsumerRecord<String, String> record) throws Exception {
        if (procesados.findByTopicAndPartitionNoAndOffsetNo(
                record.topic(), record.partition(), record.offset()).isPresent()) {
            return;
        }

        var dto = om.readValue(record.value(), SolicitudDtos.SolicitudDonaciones.class);

        String solicitudId = dto.getSolicitud_id();
        Integer orgId = dto.getOrg_id() != null ? dto.getOrg_id() : 0;

        var fecha = (dto.getFecha_hora() != null && !dto.getFecha_hora().isBlank())
                ? OffsetDateTime.parse(dto.getFecha_hora(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime()
                : null;

        var se = solicitudes.findBySolicitudId(solicitudId).orElseGet(SolicitudExterna::new);
        se.setSolicitudId(solicitudId);
        se.setOrgId(orgId);
        se.setFechaHora(fecha);
        se.setEstado("VIGENTE");
        se.setPayloadJson(record.value());
        solicitudes.save(se);

        for (var i : items.findBySolicitudId(solicitudId)) {
            items.deleteById(i.getId());
        }
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

        var mp = new MensajeProcesado();
        mp.setTopic(record.topic());
        mp.setMessageKey(record.key());
        mp.setPartitionNo(record.partition());
        mp.setOffsetNo(record.offset());
        procesados.save(mp);
    }
}
