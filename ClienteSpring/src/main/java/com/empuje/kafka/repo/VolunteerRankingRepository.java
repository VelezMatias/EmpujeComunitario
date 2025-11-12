package com.empuje.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class VolunteerRankingRepository {

    private final JdbcTemplate jdbc;

    public VolunteerRankingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SQL_INTERNOS = """
        SELECT
          u.id   AS usuarioId,
          u.nombre AS nombre,
          u.apellido AS apellido,
          COUNT(*) AS participaciones
        FROM eventos e
        JOIN evento_participantes ep ON ep.evento_id = e.id
        JOIN usuarios u ON u.id = ep.usuario_id
        WHERE e.fecha_hora >= ?         -- from inclusive
          AND e.fecha_hora <  ?         -- to exclusive
          AND e.fecha_hora < NOW()      -- NO contar futuros
        GROUP BY u.id, u.nombre, u.apellido
        ORDER BY participaciones DESC, apellido, nombre
        LIMIT ?
        """;

    private static final String SQL_EXTERNOS = """
        SELECT
          u.id   AS usuarioId,
          u.nombre AS nombre,
          u.apellido AS apellido,
          COUNT(*) AS participaciones
        FROM eventos_externos ee
        JOIN evento_externo_participantes eep ON eep.evento_externo_id = ee.id
        JOIN usuarios u ON u.id = eep.usuario_id
        WHERE ee.fecha_hora >= ?
          AND ee.fecha_hora <  ?
          AND ee.fecha_hora < NOW()
        GROUP BY u.id, u.nombre, u.apellido
        ORDER BY participaciones DESC, apellido, nombre
        LIMIT ?
        """;

    private static final String SQL_AMBOS = """
        SELECT
          usuarioId, nombre, apellido, SUM(participaciones) AS participaciones
        FROM (
            SELECT
              u.id AS usuarioId, u.nombre, u.apellido, COUNT(*) AS participaciones
            FROM eventos e
            JOIN evento_participantes ep ON ep.evento_id = e.id
            JOIN usuarios u ON u.id = ep.usuario_id
            WHERE e.fecha_hora >= ?
              AND e.fecha_hora <  ?
              AND e.fecha_hora < NOW()
            GROUP BY u.id, u.nombre, u.apellido

            UNION ALL

            SELECT
              u.id AS usuarioId, u.nombre, u.apellido, COUNT(*) AS participaciones
            FROM eventos_externos ee
            JOIN evento_externo_participantes eep ON eep.evento_externo_id = ee.id
            JOIN usuarios u ON u.id = eep.usuario_id
            WHERE ee.fecha_hora >= ?
              AND ee.fecha_hora <  ?
              AND ee.fecha_hora < NOW()
            GROUP BY u.id, u.nombre, u.apellido
        ) t
        GROUP BY usuarioId, nombre, apellido
        ORDER BY participaciones DESC, apellido, nombre
        LIMIT ?
        """;

    public List<Map<String, Object>> rank(LocalDateTime desdeIncl,
                                          LocalDateTime hastaExcl,
                                          String tipoEvento,
                                          int topN) {
        int top = Math.max(3, topN); // mínimo 3, por consigna

        return switch (tipoEvento) {
            case "INTERNOS" -> jdbc.query(SQL_INTERNOS,
                    (rs, rowNum) -> mapRow(rs),
                    desdeIncl, hastaExcl, top);

            case "EXTERNOS" -> jdbc.query(SQL_EXTERNOS,
                    (rs, rowNum) -> mapRow(rs),
                    desdeIncl, hastaExcl, top);

            case "AMBOS" -> jdbc.query(SQL_AMBOS,
                    (rs, rowNum) -> mapRow(rs),
                    // internos
                    desdeIncl, hastaExcl,
                    // externos
                    desdeIncl, hastaExcl,
                    top);

            default -> throw new IllegalArgumentException("tipoEvento inválido: " + tipoEvento);
        };
    }

    private static Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("usuarioId", rs.getInt("usuarioId"));
        m.put("nombre", rs.getString("nombre"));
        m.put("apellido", rs.getString("apellido"));
        m.put("participaciones", rs.getInt("participaciones"));
        return m;
    }
}
