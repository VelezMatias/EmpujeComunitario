package com.empuje.repo;

import com.empuje.kafka.dto.SolicitudDtos.DonationGroup;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcDonationReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDonationReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================
    // Agrupado de donaciones con filtros
    // =========================================================
    public List<DonationGroup> findGrouped(
            String categoria,
            LocalDate from,
            LocalDate to,
            String eliminado) {

        StringBuilder sql = new StringBuilder(
            "SELECT c.nombre AS categoria, " +
            "CASE WHEN d.eliminado = 1 THEN 'SI' ELSE 'NO' END AS eliminado, " +
            "COALESCE(SUM(d.cantidad), 0) AS total " +
            "FROM donaciones d " +
            "INNER JOIN categorias c ON d.categoria_id = c.id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // --- Filtro por categoría ---
        if (categoria != null && !categoria.isBlank()) {
            sql.append(" AND c.nombre LIKE ? ");
            params.add("%" + categoria + "%");
        }

        // --- Filtro por fechas ---
        if (from != null) {
            sql.append(" AND DATE(d.fecha_alta) >= ? ");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND DATE(d.fecha_alta) <= ? ");
            params.add(to);
        }

        // --- Filtro por eliminado ---
        if (eliminado != null && !"AMBOS".equalsIgnoreCase(eliminado)) {
            sql.append(" AND d.eliminado = ? ");
            params.add("SI".equalsIgnoreCase(eliminado) ? 1 : 0);
        }

        sql.append(" GROUP BY c.nombre, d.eliminado ORDER BY c.nombre;");

        System.out.println("[DEBUG SQL AGRUPADO] " + sql);
        System.out.println("[DEBUG PARAMS AGRUPADO] " + params);

        return jdbcTemplate.query(sql.toString(), params.toArray(), this::mapDonationGroup);
    }

    private DonationGroup mapDonationGroup(ResultSet rs, int rowNum) throws SQLException {
        DonationGroup group = new DonationGroup();
        group.setCategoria(rs.getString("categoria"));
        group.setEliminado(rs.getString("eliminado"));

        
        try {
            group.setTotal(rs.getBigDecimal("total"));
        } catch (Exception e) {
            group.setTotal(java.math.BigDecimal.valueOf(rs.getDouble("total")));
        }

        return group;
    }

    // =========================================================
    // Detalle individual con filtros
    // =========================================================
    public List<Map<String, Object>> obtenerDonacionesIndividuales(
            String categoria,
            String eliminado,
            LocalDate from,
            LocalDate to) {

        StringBuilder sql = new StringBuilder(
            "SELECT d.descripcion, d.cantidad, d.fecha_alta " +
            "FROM donaciones d " +
            "INNER JOIN categorias c ON d.categoria_id = c.id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // --- Filtro categoría (case-insensitive y tolerando '_') ---
        if (categoria != null && !categoria.isBlank()) {
            sql.append(" AND REPLACE(UPPER(c.nombre), '_', ' ') LIKE CONCAT('%', REPLACE(UPPER(?), '_', ' '), '%') ");
            params.add(categoria);
        }

        // --- Filtro eliminado ---
        if (eliminado != null && !"AMBOS".equalsIgnoreCase(eliminado)) {
            sql.append(" AND d.eliminado = ? ");
            params.add("SI".equalsIgnoreCase(eliminado) ? 1 : 0);
        }

        // --- Filtro fechas ---
        if (from != null) {
            sql.append(" AND DATE(d.fecha_alta) >= ? ");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND DATE(d.fecha_alta) <= ? ");
            params.add(to);
        }

        sql.append(" ORDER BY d.fecha_alta DESC;");

        System.out.println("[DEBUG SQL DETALLES] " + sql);
        System.out.println("[DEBUG PARAMS DETALLES] " + params);

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }
}
