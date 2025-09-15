package com.empuje.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.empuje.kafka.Payloads.*;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper om;
    private final int appOrgId;

    public KafkaProducerService(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper om,
            @Value("${app.org-id}") int appOrgId
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.om = om;
        this.appOrgId = appOrgId;
    }

    // ---------- Helpers ----------
    private static String orDefault(String value, String def) {
        return (value == null || value.isBlank()) ? def : value;
    }

    private String toJson(Object payload) {
        try {
            return om.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando payload a JSON", e);
        }
    }

    private void send(String topic, String key, Object payload) {
        String json = toJson(payload);
        kafkaTemplate.send(topic, key, json).whenComplete((res, ex) -> {
            if (ex != null) {
                System.err.println("[KAFKA][ERROR] topic=" + topic + " key=" + key + " -> " + ex.getMessage());
            } else {
                System.out.println("[KAFKA][OK] topic=" + topic + " key=" + key + " -> " + json);
            }
        });
    }

    // ---------- Publicadores ----------

    // 1) solicitud-donaciones (key = solicitud_id)
    public void publishSolicitud(SolicitudDonaciones p) {
        if (p.orgId == null) p.orgId = appOrgId;
        if (p.idempotencyKey == null) p.idempotencyKey = p.orgId + ":" + p.solicitudId;
        send(KafkaTopics.SOLICITUD_DONACIONES, p.solicitudId, p);
    }

    // 2) baja-solicitud-donaciones (key = solicitud_id)
    public void publishBajaSolicitud(BajaSolicitudDonaciones p) {
        if (p.orgId == null) p.orgId = appOrgId;
        if (p.idempotencyKey == null) p.idempotencyKey = p.orgId + ":" + p.solicitudId + ":BAJA";
        send(KafkaTopics.BAJA_SOLICITUD_DONACIONES, p.solicitudId, p);
    }

    // 3) oferta-donaciones (key = oferta_id)
    public void publishOferta(OfertaDonaciones p) {
        if (p.orgId == null) p.orgId = appOrgId;
        if (p.idempotencyKey == null) p.idempotencyKey = p.orgId + ":" + p.ofertaId;
        send(KafkaTopics.OFERTA_DONACIONES, p.ofertaId, p);
    }

    // 4) eventos-solidarios (key = evento_id)
    public void publishEvento(EventoSolidario p) {
        if (p.orgId == null) p.orgId = appOrgId;
        if (p.idempotencyKey == null) p.idempotencyKey = p.orgId + ":" + p.eventoId;
        send(KafkaTopics.EVENTOS_SOLIDARIOS, p.eventoId, p);
    }

    // 5) baja-evento-solidario (key = evento_id)
    public void publishBajaEvento(BajaEventoSolidario p) {
        if (p.orgId == null) p.orgId = appOrgId;
        if (p.idempotencyKey == null) p.idempotencyKey = p.orgId + ":" + p.eventoId + ":BAJA";
        send(KafkaTopics.BAJA_EVENTO_SOLIDARIO, p.eventoId, p);
    }

    // 6) adhesion-evento.<orgIdOrganizador> (key = evento_id)  [dinámico]
    public void publishAdhesion(AdhesionEvento p) {
        // orgIdAdherente por defecto es el nuestro (appOrgId)
        if (p.orgIdAdherente == null) p.orgIdAdherente = appOrgId;
        if (p.idempotencyKey == null) p.idempotencyKey = p.orgIdAdherente + ":" + p.eventoId + ":ADH";
        String topic = KafkaTopics.adhesionEvento(p.orgIdOrganizador != null ? p.orgIdOrganizador : appOrgId);
        send(topic, p.eventoId, p);
    }
}
