package com.empuje.graphql;

import com.empuje.repo.JdbcEventReportRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugEventReportController {

    private final JdbcEventReportRepository repo;

    public DebugEventReportController(JdbcEventReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/event-report")
    public ResponseEntity<?> getReport(
            @RequestParam(name = "usuarioId") Integer usuarioId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "repartoDonaciones", required = false, defaultValue = "AMBOS") String repartoDonaciones
    ) {
        if (usuarioId == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "usuarioId is required");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            List<Map<String, Object>> events = repo.findEventsForUser(usuarioId, from, to, repartoDonaciones);
            for (Map<String, Object> ev : events) {
                Object idObj = ev.get("id");
                Integer eid = null;
                if (idObj instanceof Integer) eid = (Integer) idObj;
                else if (idObj instanceof Number) eid = ((Number) idObj).intValue();
                if (eid != null) {
                    List<Map<String, Object>> don = repo.findDonationsForEvent(eid);
                    ev.put("donaciones", don);
                }
            }

            return ResponseEntity.ok(events);

        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Exception: " + e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }
}
