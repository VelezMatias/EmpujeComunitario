package com.empuje.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.empuje.repo.VolunteerRankingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class VolunteerRankingResolver {

    private final VolunteerRankingRepository repo;

    public VolunteerRankingResolver(VolunteerRankingRepository repo) {
        this.repo = repo;
    }

    @QueryMapping
    public List<Map<String, Object>> volunteerRanking(@Argument("filtro") VolunteerRankFilterInput filtro) {
        // Evita NPE si llaman sin filtro
        if (filtro == null) {
            throw new IllegalArgumentException("El filtro es obligatorio");
        }

        // Parseo y ventana half-open [from, to)
        LocalDate from = LocalDate.parse(filtro.getFrom()); // YYYY-MM-DD
        LocalDate to = LocalDate.parse(filtro.getTo());

        LocalDateTime desdeIncl = from.atStartOfDay();
        LocalDateTime hastaExcl = to.plusDays(1).atStartOfDay();

        String tipo = filtro.getTipo().name(); // INTERNOS | EXTERNOS | AMBOS
        int top = Math.max(3, filtro.getTopN());

        return repo.rank(desdeIncl, hastaExcl, tipo, top);
    }
}
