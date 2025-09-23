package com.empuje.ui.solicitudes;

import com.empuje.kafka.dto.SolicitudDtos;
import com.empuje.kafka.dto.SolicitudDtos.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Controller
@RequestMapping("/ui/solicitudes")
public class SolicitudesUIController {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper om = new ObjectMapper();
    private static final SecureRandom RNG = new SecureRandom();

    public SolicitudesUIController(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        // dto inicial: 1 renglón vacío para que el usuario arranque
        var dto = new SolicitudDtos.SolicitudDonaciones();
        dto.setItems(new ArrayList<>());
        Item it = new Item();
        it.setCategoria("ALIMENTOS");
        it.setUnidad("u");
        it.setCantidad(1);
        dto.getItems().add(it);

        // timestamp sugerido (editable o se ignora si lo querés)
        dto.setFecha_hora(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        model.addAttribute("dto", dto);
        model.addAttribute("mensaje", null);
        model.addAttribute("error", null);
        return "solicitudes/form";
    }

    @PostMapping
    public String publicar(@ModelAttribute("dto") SolicitudDtos.SolicitudDonaciones dto, Model model) {
        try {
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                throw new IllegalArgumentException("Debe cargar al menos un ítem.");
            }

            // si no vino solicitud_id, lo generamos en el backend
            if (!StringUtils.hasText(dto.getSolicitud_id())) {
                dto.setSolicitud_id(generarSolicitudId());
            }

            // si no vino idempotency_key, usamos el mismo ID de solicitud
            if (!StringUtils.hasText(dto.getIdempotency_key())) {
                dto.setIdempotency_key(dto.getSolicitud_id());
            }

            String json = om.writeValueAsString(dto);
            kafka.send("solicitud-donaciones", dto.getIdempotency_key(), json);

            model.addAttribute("mensaje", "Publicado en Kafka. ID: " + dto.getSolicitud_id());
            model.addAttribute("error", null);
        } catch (Exception ex) {
            model.addAttribute("mensaje", null);
            model.addAttribute("error", ex.getMessage());
        }
        return "solicitudes/form";
    }

    // Ej: SOL-2025-8F3A12C9
    private String generarSolicitudId() {
        String year = String.valueOf(OffsetDateTime.now().getYear());
        String hex = Integer.toHexString(RNG.nextInt()).toUpperCase();
        
        String sufijo = hex.replace("-", "");
        if (sufijo.length() < 8) {
            sufijo = (sufijo + "00000000").substring(0, 8);
        } else {
            sufijo = sufijo.substring(0, 8);
        }
        return "SOL-" + year + "-" + sufijo;
    }
}
