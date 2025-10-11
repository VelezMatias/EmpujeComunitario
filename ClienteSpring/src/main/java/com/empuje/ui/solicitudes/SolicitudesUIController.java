package com.empuje.ui.solicitudes;

import com.empuje.kafka.config.OrgConfig;
import com.empuje.kafka.dto.SolicitudDtos;
import com.empuje.kafka.dto.SolicitudDtos.Item;
import com.empuje.kafka.entity.Solicitud;
import com.empuje.kafka.repo.SolicitudRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ui/solicitudes")
public class SolicitudesUIController {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper om = new ObjectMapper();
    private final OrgConfig org;
    private final SolicitudRepository solicitudes;
    private final JdbcTemplate jdbc; // para items y cumplidas
    private static final SecureRandom RNG = new SecureRandom();

    public SolicitudesUIController(KafkaTemplate<String, String> kafka,
                                   OrgConfig org,
                                   SolicitudRepository solicitudes,
                                   JdbcTemplate jdbc) {
        this.kafka = kafka;
        this.org = org;
        this.solicitudes = solicitudes;
        this.jdbc = jdbc;
    }

    // ------------------- MODEL ATTRIBUTES (columna derecha) -------------------

    /** Mis solicitudes (PROPIAS) ordenadas por fecha desc. */
    @ModelAttribute("propias")
    public List<Solicitud> cargarPropias() {
        // Sin depender de métodos extra en el repo: filtro en memoria y ordeno.
        List<Solicitud> all = solicitudes.findAll();
        Integer myOrg = org.getOrgId();
        return all.stream()
                .filter(s -> s.getOrgId() != null && Objects.equals(s.getOrgId(), myOrg))
                .sorted(Comparator.comparing(Solicitud::getFechaHora,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    /** Ítems de mis solicitudes (mapa solicitudId -> lista de items). */
    @ModelAttribute("propItemsBy")
    public Map<String, List<ItemVM>> cargarPropItems(@ModelAttribute("propias") List<Solicitud> propias) {
        if (propias == null || propias.isEmpty()) return Collections.emptyMap();
        List<String> ids = propias.stream().map(Solicitud::getSolicitudId).filter(Objects::nonNull).toList();

        String placeholders = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT solicitud_id, categoria, descripcion, cantidad " +
                     "FROM solicitud_prop_items WHERE solicitud_id IN (" + placeholders + ") ORDER BY id ASC";

        List<Row> rows = jdbc.query(sql, ids.toArray(), (rs, i) -> new Row(
                rs.getString("solicitud_id"),
                rs.getString("categoria"),
                rs.getString("descripcion"),
                (Integer) rs.getObject("cantidad")
        ));

        Map<String, List<ItemVM>> out = new LinkedHashMap<>();
        for (Row r : rows) {
            out.computeIfAbsent(r.sid, k -> new ArrayList<>())
               .add(new ItemVM(r.categoria, r.descripcion, r.cantidad));
        }
        return out;
    }

    /** Lista de IDs cumplidos para mostrar badge/ocultar botón. */
    @ModelAttribute("cumplidas")
    public List<String> cargarCumplidas() {
        return jdbc.query("SELECT solicitud_id FROM solicitudes_cumplidas",
                (rs, i) -> rs.getString(1));
    }

    // ------------------- NUEVA / PUBLICAR -------------------

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

            // Fecha ISO con offset (ej. 2025-10-09T19:05:00-03:00)
            if (!StringUtils.hasText(dto.getFecha_hora())) {
                dto.setFecha_hora(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }

            String json = om.writeValueAsString(dto);

            // 1) Publicar a Kafka (key = ORG_ID)
            kafka.send("solicitud-donaciones", String.valueOf(org.getOrgId()), json);

            // 2) Guardar como PROPIA en BD (para la UI)
            var row = solicitudes.findBySolicitudId(dto.getSolicitud_id()).orElseGet(Solicitud::new);
            row.setSolicitudId(dto.getSolicitud_id());
            row.setOrgId(org.getOrgId());

            // Guardar como LocalDateTime (sin offset)
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

    // ------------------- ELIMINAR PROPIA -------------------

    @PostMapping("/{solicitudId}/eliminar")
    public String eliminarPropia(@PathVariable String solicitudId, RedirectAttributes ra) {
        try {
            // 0) ¿Existe?
            var opt = solicitudes.findBySolicitudId(solicitudId);
            if (opt.isEmpty()) {
                ra.addFlashAttribute("error", "No existe la solicitud " + solicitudId + ".");
                return "redirect:/ui/solicitudes";
            }
            var row = opt.get();

            // 1) ¿Pertenece a mi organización?
            if (row.getOrgId() == null || !Objects.equals(row.getOrgId(), org.getOrgId())) {
                ra.addFlashAttribute("error", "La solicitud no pertenece a tu organización.");
                return "redirect:/ui/solicitudes";
            }

            // 2) ¿Está CUMPLIDA?
            if (isCumplida(solicitudId)) {
                ra.addFlashAttribute("error", "La solicitud " + solicitudId + " ya está CUMPLIDA y no puede eliminarse.");
                return "redirect:/ui/solicitudes";
            }

            // 3) Limpio marca de cumplida por las dudas
            deleteCumplida(solicitudId);

            // 4) Eliminar de 'solicitudes' (por FK ON DELETE CASCADE se van los ítems)
            solicitudes.delete(row);

            ra.addFlashAttribute("msg", "Solicitud " + solicitudId + " eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/ui/solicitudes";
    }

    private boolean isCumplida(String solicitudId) {
        Integer flag = jdbc.query(
                "SELECT 1 FROM solicitudes_cumplidas WHERE solicitud_id = ? LIMIT 1",
                ps -> ps.setString(1, solicitudId),
                rs -> rs.next() ? 1 : 0
        );
        return flag != null && flag == 1;
    }

    private void deleteCumplida(String solicitudId) {
        jdbc.update("DELETE FROM solicitudes_cumplidas WHERE solicitud_id = ?", solicitudId);
    }

    // ------------------- UTIL -------------------

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

    // helpers internos para mapear filas
    private static final class Row {
        final String sid;
        final String categoria;
        final String descripcion;
        final Integer cantidad;
        Row(String sid, String categoria, String descripcion, Integer cantidad) {
            this.sid = sid; this.categoria = categoria; this.descripcion = descripcion; this.cantidad = cantidad;
        }
    }
    public static final class ItemVM {
        public final String categoria;
        public final String descripcion;
        public final Integer cantidad;
        public ItemVM(String c, String d, Integer n) { this.categoria = c; this.descripcion = d; this.cantidad = n; }
    }
}
