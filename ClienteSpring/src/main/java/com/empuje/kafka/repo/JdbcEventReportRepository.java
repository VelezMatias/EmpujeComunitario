package com.empuje.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcEventReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEventReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findEventsForUser(
            Integer usuarioId,
            LocalDate from,
            LocalDate to,
            String repartoDonaciones) {

        StringBuilder sql = new StringBuilder(
                "SELECT e.id, e.nombre, e.descripcion, e.fecha_hora, COALESCE(COUNT(ed.donacion_id),0) AS donation_count " +
                        "FROM eventos e " +
                        "INNER JOIN evento_participantes ep ON ep.evento_id = e.id " +
                        "LEFT JOIN evento_donacion ed ON ed.evento_id = e.id " +
                        "WHERE ep.usuario_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(usuarioId);

        if (from != null) {
            sql.append(" AND DATE(e.fecha_hora) >= ? ");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND DATE(e.fecha_hora) <= ? ");
            params.add(to);
        }

        sql.append(" GROUP BY e.id, e.nombre, e.descripcion, e.fecha_hora ");

        //HAVING y filtro "repartoDonaciones"
        if (repartoDonaciones != null && !"AMBOS".equalsIgnoreCase(repartoDonaciones)) {
            if ("SI".equalsIgnoreCase(repartoDonaciones)) {
                sql.append(" HAVING donation_count > 0 ");
            } else if ("NO".equalsIgnoreCase(repartoDonaciones)) {
                sql.append(" HAVING donation_count = 0 ");
            }
        }

        sql.append(" ORDER BY e.fecha_hora DESC");

        System.out.println("[DEBUG SQL EVENTS] " + sql);
        System.out.println("[DEBUG PARAMS EVENTS] " + params);

        return jdbcTemplate.query(sql.toString(), params.toArray(), (ResultSet rs, int rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", rs.getInt("id"));
            row.put("nombre", rs.getString("nombre"));
            row.put("descripcion", rs.getString("descripcion"));
            row.put("fecha_hora", rs.getTimestamp("fecha_hora"));
            row.put("donation_count", rs.getInt("donation_count"));
            return row;
        });
    }

    public List<Map<String, Object>> findDonationsForEvent(Integer eventoId) {
        String sql = "SELECT d.descripcion, COALESCE(ed.cantidad,0) AS cantidad " +
                "FROM evento_donacion ed " +
                "JOIN donaciones d ON d.id = ed.donacion_id " +
                "WHERE ed.evento_id = ? ";

        return jdbcTemplate.queryForList(sql, eventoId);
    }

    //util
    private LocalDateTime parseDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        }
        try {
            return LocalDateTime.parse(obj.toString(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}
