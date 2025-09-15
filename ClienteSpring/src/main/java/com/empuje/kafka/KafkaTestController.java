package com.empuje.kafka;

import com.empuje.kafka.Payloads.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kafka")
public class KafkaTestController {

    private final KafkaProducerService svc;

    public KafkaTestController(KafkaProducerService svc) {
        this.svc = svc;
    }

    @GetMapping("/ping")
    public String ping() { return "pong"; }

    @PostMapping("/solicitud")
    public ResponseEntity<String> solicitud(@RequestBody SolicitudDonaciones p) {
        svc.publishSolicitud(p);
        return ResponseEntity.ok("Publicado en Kafka: " + p.solicitudId);
    }
}
