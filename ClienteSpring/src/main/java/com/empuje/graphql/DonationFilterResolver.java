package com.empuje.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class DonationFilterResolver {

    private static final String ATTR_LIST = "donationFilters";     // List<DonationFilter>
    private static final String ATTR_NEXT = "donationFiltersNext"; // AtomicLong

    /* ===================== Helpers de sesión ===================== */

    private HttpSession session() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest().getSession(true) : null;
    }

    @SuppressWarnings("unchecked")
    private List<DonationFilter> getList() {
        HttpSession s = session();
        if (s == null) return new ArrayList<>();
        Object v = s.getAttribute(ATTR_LIST);
        if (v instanceof List) return (List<DonationFilter>) v;
        List<DonationFilter> list = new ArrayList<>();
        s.setAttribute(ATTR_LIST, list);
        return list;
    }

    private AtomicLong getNext() {
        HttpSession s = session();
        if (s == null) return new AtomicLong(1L);
        Object v = s.getAttribute(ATTR_NEXT);
        if (v instanceof AtomicLong) return (AtomicLong) v;
        AtomicLong next = new AtomicLong(1L);
        s.setAttribute(ATTR_NEXT, next);
        return next;
    }

    /* ========================= Query ========================= */

    @QueryMapping
    public List<DonationFilter> myFilters() {
        return new ArrayList<>(getList());
    }

    /* ======================== Mutations ====================== */

    public static class FilterInput {
        private String nombre;
        private String categoria;
        private String from;
        private String to;
        private String eliminado;

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

    @MutationMapping
    public DonationFilter saveFilter(@Argument FilterInput input) {
        String nombre = safe(input.getNombre());
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre del filtro es obligatorio.");

        DonationFilter f = new DonationFilter(
                getNext().getAndIncrement(),
                nombre,
                nullIfEmpty(input.getCategoria()),
                nullIfEmpty(input.getFrom()),
                nullIfEmpty(input.getTo()),
                nullIfEmpty(input.getEliminado())
        );
        List<DonationFilter> list = getList();
        list.add(f);
        return f;
    }

    @MutationMapping
    public DonationFilter updateFilter(@Argument long id, @Argument FilterInput input) {
        List<DonationFilter> list = getList();
        DonationFilter existing = list.stream()
                .filter(x -> x.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Filtro no encontrado"));

        if (input.getNombre() != null && !safe(input.getNombre()).isEmpty()) {
            existing.setNombre(input.getNombre().trim());
        }
        existing.setCategoria(nullIfEmpty(input.getCategoria()));
        existing.setFrom(nullIfEmpty(input.getFrom()));
        existing.setTo(nullIfEmpty(input.getTo()));
        existing.setEliminado(nullIfEmpty(input.getEliminado()));

        return existing;
    }

    @MutationMapping
    public boolean deleteFilter(@Argument long id) {
        List<DonationFilter> list = getList();
        return list.removeIf(x -> x.getId() == id);
    }

    /* ===================== Utils ===================== */

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static String nullIfEmpty(String s) {
        s = safe(s);
        return s.isEmpty() ? null : s;
    }
}
