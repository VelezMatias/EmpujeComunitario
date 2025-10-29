package com.empuje.graphql;

import com.empuje.repo.JdbcEventReportRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EventReportResolver {

    private static final Logger log = LoggerFactory.getLogger(EventReportResolver.class);

    private final JdbcEventReportRepository repo;

    public EventReportResolver(JdbcEventReportRepository repo) {
        this.repo = repo;
    }

    @QueryMapping
    public List<Map<String, Object>> eventParticipationReport(
            @Argument Integer usuarioId,
            @Argument String from,
            @Argument String to,
            @Argument String repartoDonaciones) {

        HttpSession session = null;
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            session = ((ServletRequestAttributes) attrs).getRequest().getSession(false);
        }

        if (session == null) {
            if (attrs instanceof ServletRequestAttributes) {
                ServletRequestAttributes sra = (ServletRequestAttributes) attrs;
                try {
                    String cookieHeader = sra.getRequest().getHeader("Cookie");
                    String remote = sra.getRequest().getRemoteAddr();
                    String uri = sra.getRequest().getRequestURI();
                    log.warn("GraphQL request without session. remote={}, uri={}, Cookie={}", remote, uri, cookieHeader);
                } catch (Exception ex) {
                    log.warn("GraphQL request without session and failed to read request info", ex);
                }
            } else {
                log.warn("No ServletRequestAttributes available when session was expected");
            }
            throw new RuntimeException("No autorizado: sesión HTTP no disponible");
        }

        LocalDate fFrom = parseDate(from);
        LocalDate fTo = parseDate(to);

        //(AuthController guarda userId y rol en session)
        Object rolObj = session.getAttribute("rol");
        boolean isPrivileged = false;
        if (rolObj != null) {
            String rol = rolObj.toString();
            if ("PRESIDENTE".equalsIgnoreCase(rol) || "COORDINADOR".equalsIgnoreCase(rol)) {
                isPrivileged = true;
            }
        }

        Object uid = session.getAttribute("userId");
        Integer principalId = null;
        if (uid instanceof Integer) principalId = (Integer) uid;
        else if (uid instanceof String) {
            try { principalId = Integer.parseInt((String) uid); } catch (Exception ignored) {}
        }

        if (!isPrivileged) {
            if (principalId == null) throw new RuntimeException("No autorizado: no se pudo identificar al usuario");
            if (!principalId.equals(usuarioId)) throw new RuntimeException("No autorizado: sólo puede consultar su propio usuario");
        }

        List<Map<String, Object>> events = repo.findEventsForUser(usuarioId, fFrom, fTo, repartoDonaciones);

        List<Map<String, Object>> enriched = new ArrayList<>();
        for (Map<String, Object> e : events) {
            Integer id = (Integer) e.get("id");
            List<Map<String, Object>> donations = repo.findDonationsForEvent(id);
            Map<String, Object> item = new HashMap<>();
            Object fh = e.get("fecha_hora");
            LocalDateTime dt = null;
            if (fh instanceof java.sql.Timestamp) dt = ((java.sql.Timestamp) fh).toLocalDateTime();
            item.put("id", id);
            item.put("fecha_hora", dt);
            item.put("nombre", e.get("nombre"));
            item.put("descripcion", e.get("descripcion"));
            item.put("donaciones", donations);
            enriched.add(item);
        }

        //Agrupa por mes/anio
        Map<String, Map<String, Object>> groups = new HashMap<>();
        for (Map<String, Object> ev : enriched) {
            LocalDateTime dt = (LocalDateTime) ev.get("fecha_hora");
            String mesKey = "unknown";
            int year = 0;
            int mes = 0;
            if (dt != null) {
                year = dt.getYear();
                mes = dt.getMonthValue();
                mesKey = String.format("%04d-%02d", year, mes);
            }

            Map<String, Object> g = groups.get(mesKey);
            if (g == null) {
                g = new HashMap<>();
                g.put("mes", String.format("%02d", mes));
                g.put("year", year);
                g.put("eventos", new ArrayList<Map<String, Object>>());
                groups.put(mesKey, g);
            }

            List<Map<String, Object>> lista = (List<Map<String, Object>>) g.get("eventos");
            Map<String, Object> item = new HashMap<>();
            item.put("dia", dt == null ? null : dt.getDayOfMonth());
            item.put("nombre", ev.get("nombre"));
            item.put("descripcion", ev.get("descripcion"));
            item.put("donaciones", ev.get("donaciones"));
            lista.add(item);
        }

        List<Map<String, Object>> out = new ArrayList<>();
        out.addAll(groups.values());
        return out;
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            System.err.println("[WARN] Fecha inválida: " + date);
            return null;
        }
    }
}
