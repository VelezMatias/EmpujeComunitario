package com.empuje.kafka;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class KafkaTestController {

    private final KafkaProducerService producer;

    public KafkaTestController(KafkaProducerService producer) {
        this.producer = producer;
    }

    @GetMapping("/kafka/ping")
    public String ping() {
        return "pong";
    }

    // -------- Solicitud ----------
    @PostMapping("/kafka/solicitud")
    public ResponseEntity<?> solicitud(@RequestBody Payloads.SolicitudDonaciones body) {
        producer.publishSolicitud(body);
        return ResponseEntity.ok("Publicado en Kafka: " + body.getSolicitud_id());
    }

    @PostMapping("/kafka/baja-solicitud")
    public ResponseEntity<?> bajaSolicitud(@RequestBody Payloads.BajaSolicitudDonaciones body) {
        producer.publishBajaSolicitud(body);
        return ResponseEntity.ok("Publicado baja solicitud: " + body.getSolicitud_id());
    }

    // -------- Evento ----------
    @PostMapping("/kafka/evento")
    public ResponseEntity<?> evento(@RequestBody Payloads.EventoSolidario body) {
        producer.publishEvento(body);
        return ResponseEntity.ok("Publicado evento: " + body.getEvento_id());
    }

    @PostMapping("/kafka/baja-evento")
    public ResponseEntity<?> bajaEvento(@RequestBody Payloads.BajaEventoSolidario body) {
        producer.publishBajaEvento(body);
        return ResponseEntity.ok("Publicado baja evento: " + body.getEvento_id());
    }
}
