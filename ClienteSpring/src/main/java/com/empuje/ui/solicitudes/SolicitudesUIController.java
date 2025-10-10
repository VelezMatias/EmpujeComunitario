package com.empuje.ui.solicitudes;

import com.empuje.kafka.config.OrgConfig;
import com.empuje.kafka.dto.SolicitudDtos;
import com.empuje.kafka.dto.SolicitudDtos.Item;
import com.empuje.kafka.entity.Solicitud;
import com.empuje.kafka.repo.SolicitudRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Controller
@RequestMapping("/ui/solicitudes")
public class SolicitudesUIController {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper om = new ObjectMapper();
    private final OrgConfig org;
    private final SolicitudRepository solicitudes;
    private static final SecureRandom RNG = new SecureRandom();

    public SolicitudesUIController(KafkaTemplate<String, String> kafka,
                                   OrgConfig org,
                                   SolicitudRepository solicitudes) {
        this.kafka = kafka;
        this.org = org;
        this.solicitudes = solicitudes;
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        var dto = new SolicitudDtos.SolicitudDonaciones();
        dto.setItems(new ArrayList<>());
        Item it = new Item();
        it.setCategoria("ALIMENTOS");
        it.setUnidad("u");
        it.setCantidad(1);
        dto.getItems().add(it);

        dto.setFecha_hora(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        dto.setOrg_id(org.getOrgId());

        model.addAttribute("dto", dto);
        model.addAttribute("mensaje", null);
        model.addAttribute("error", null);
        return "solicitudes/form";
    }

    @PostMapping
    public String publicar(@ModelAttribute("dto") SolicitudDtos.SolicitudDonaciones dto, Model model) {
        try {
            if (dto.getItems() == null || dto.getItems().isEmpty())
                throw new IllegalArgumentException("Debe cargar al menos un ítem.");

            if (!StringUtils.hasText(dto.getSolicitud_id()))
                dto.setSolicitud_id(generarSolicitudId());

            if (!StringUtils.hasText(dto.getIdempotency_key()))
                dto.setIdempotency_key(dto.getSolicitud_id());

            // Fuerzo mi ORG_ID
            dto.setOrg_id(org.getOrgId());

            // Fecha en formato ISO con offset (ej. 2025-10-09T19:05:00-03:00)
            if (!StringUtils.hasText(dto.getFecha_hora())) {
                dto.setFecha_hora(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }

            String json = om.writeValueAsString(dto);

            // 1) Publicar a Kafka (key = mi ORG_ID)
            kafka.send("solicitud-donaciones", String.valueOf(org.getOrgId()), json);

            // 2) Guardar “propia” en BD (para que aparezca en la UI)
            var row = solicitudes.findBySolicitudId(dto.getSolicitud_id()).orElseGet(Solicitud::new);
            row.setSolicitudId(dto.getSolicitud_id());
            row.setOrgId(org.getOrgId());

            // ⇩ Guardar como LocalDateTime para que no se corra la hora
            LocalDateTime fh;
            try {
                fh = OffsetDateTime.parse(dto.getFecha_hora(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
            } catch (Exception ignore) {
                fh = LocalDateTime.now();
            }
            row.setFechaHora(fh);

            row.setPayloadJson(json);
            solicitudes.save(row);

            model.addAttribute("mensaje", "Publicado y guardado. ID: " + dto.getSolicitud_id());
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
