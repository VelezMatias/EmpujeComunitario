package com.empuje.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GraphQL (sin modificar BD) para guardar/editar/borrar filtros por usuario.
 * Se requiere 'username' y 'rol' en la sesión.
 * Solo PRESIDENTE/VOCAL pueden modificar y eliminar; cualquiera puede leer myFilters.
 **/

@Controller
public class DonationFilterResolver {

    // username -> (id -> SavedFilter) ; memoria del proceso (sin BD)
    private final Map<String, Map<String, SavedFilter>> store = new ConcurrentHashMap<>();
    private static final Set<String> ALLOW_ROLES = Set.of("PRESIDENTE", "VOCAL");

    private String username(HttpSession session) {
        Object u = session.getAttribute("username");
        return (u != null) ? u.toString() : "anon";
    }

    private String role(HttpSession session) {
        Object r = session.getAttribute("rol");
        return (r != null) ? r.toString() : "";
    }

    private void ensureCanWrite(HttpSession session) {
        if (!ALLOW_ROLES.contains(role(session))) {
            throw new RuntimeException("No tiene permisos para guardar/editar/borrar filtros.");
        }
    }

    // ===== Query =====

    @QueryMapping
    public List<SavedFilter> myFilters(HttpSession session) {
        String user = username(session);
        return new ArrayList<>(store.getOrDefault(user, Map.of()).values())
                .stream()
                .sorted(Comparator.comparing(SavedFilter::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // ===== Mutations =====

    @MutationMapping
    public SavedFilter saveFilter(@Argument FilterInput input, HttpSession session) {
        ensureCanWrite(session);
        String user = username(session);
        String id = String.valueOf(Instant.now().toEpochMilli());

        SavedFilter f = new SavedFilter();
        f.setId(id);
        f.setNombre(input.getNombre().trim());
        f.setCategoria(nz(input.getCategoria()));
        f.setFrom(nz(input.getFrom()));
        f.setTo(nz(input.getTo()));
        f.setEliminado(nz(input.getEliminado()));
        f.setUsername(user);

        store.computeIfAbsent(user, k -> new ConcurrentHashMap<>()).put(id, f);
        return f;
    }

    @MutationMapping
    public SavedFilter updateFilter(@Argument String id, @Argument FilterInput input, HttpSession session) {
        ensureCanWrite(session);
        String user = username(session);
        Map<String, SavedFilter> byId = store.getOrDefault(user, Map.of());
        SavedFilter existing = byId.get(id);
        if (existing == null) throw new RuntimeException("Filtro inexistente para este usuario.");

        existing.setNombre(input.getNombre().trim());
        existing.setCategoria(nz(input.getCategoria()));
        existing.setFrom(nz(input.getFrom()));
        existing.setTo(nz(input.getTo()));
        existing.setEliminado(nz(input.getEliminado()));
        return existing;
    }

    @MutationMapping
    public Boolean deleteFilter(@Argument String id, HttpSession session) {
        ensureCanWrite(session);
        String user = username(session);
        Map<String, SavedFilter> byId = store.getOrDefault(user, Map.of());
        return byId.remove(id) != null;
    }

    private static String nz(String s) { return (s == null) ? "" : s; }

    // ===== POJOs mapeados con el schema =====

    public static class FilterInput {
        private String nombre, categoria, from, to, eliminado;
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getEliminado() { return eliminado; }
        public void setEliminado(String eliminado) { this.eliminado = eliminado; }
    }

    public static class SavedFilter {
        private String id, nombre, categoria, from, to, eliminado, username;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getEliminado() { return eliminado; }
        public void setEliminado(String eliminado) { this.eliminado = eliminado; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
}