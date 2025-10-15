package com.empuje.ui;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class CategoriaController {

    private final JdbcTemplate jdbcTemplate;

    public CategoriaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/categorias")
    public List<Map<String, Object>> listarCategorias() {
        return jdbcTemplate.queryForList("SELECT nombre FROM categorias ORDER BY nombre;");
    }
}
