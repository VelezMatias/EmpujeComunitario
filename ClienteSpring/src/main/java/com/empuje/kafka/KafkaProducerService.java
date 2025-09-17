package com.empuje.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // Topics
    private static final String TOPIC_SOLICITUD = "solicitud-donaciones";
    private static final String TOPIC_BAJA_SOLICITUD = "baja-solicitud-donaciones";
    private static final String TOPIC_EVENTO = "eventos-solidarios";
    private static final String TOPIC_BAJA_EVENTO = "baja-evento-solidario";

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // ---------- Solicitud ----------
    public void publishSolicitud(Payloads.SolicitudDonaciones s) {
        sendJson(TOPIC_SOLICITUD, s.getSolicitud_id(), s);
    }

    public void publishBajaSolicitud(Payloads.BajaSolicitudDonaciones s) {
        String key = "BAJA:" + s.getSolicitud_id();
        sendJson(TOPIC_BAJA_SOLICITUD, key, s);
    }

    // ---------- Evento ----------
    public void publishEvento(Payloads.EventoSolidario e) {
        sendJson(TOPIC_EVENTO, e.getEvento_id(), e);
    }

    public void publishBajaEvento(Payloads.BajaEventoSolidario e) {
        String key = "BAJA:" + e.getEvento_id();
        sendJson(TOPIC_BAJA_EVENTO, key, e);
    }

    // ---------- util ----------
    private void sendJson(String topic, String key, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            kafkaTemplate.send(topic, key, json);
            System.out.printf("[KAFKA][OK] topic=%s key=%s -> %s%n", topic, key, json);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Error serializando JSON para topic " + topic, ex);
        }
    }
}
