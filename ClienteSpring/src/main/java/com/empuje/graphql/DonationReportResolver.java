package com.empuje.graphql;

import com.empuje.kafka.dto.SolicitudDtos.DonationGroup;
import com.empuje.repo.JdbcDonationReportRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class DonationReportResolver {

    private final JdbcDonationReportRepository donationRepo;

    public DonationReportResolver(JdbcDonationReportRepository donationRepo) {
        this.donationRepo = donationRepo;
    }

    // --- Informe agrupado ---
    @QueryMapping
    public List<DonationGroup> donationReportGrouped(
            @Argument String categoria,
            @Argument String from,
            @Argument String to,
            @Argument String eliminado) {

        LocalDate fFrom = parseDate(from);
        LocalDate fTo   = parseDate(to);

        return donationRepo.findGrouped(categoria, fFrom, fTo, eliminado);
    }

    // --- Detalle individual ---
    @QueryMapping
    public List<Map<String, Object>> donationDetails(
            @Argument String categoria,
            @Argument String eliminado,
            @Argument String from,
            @Argument String to) {

        LocalDate fFrom = parseDate(from);
        LocalDate fTo   = parseDate(to);

        return donationRepo.obtenerDonacionesIndividuales(categoria, eliminado, fFrom, fTo);
    }

    // --- Conversión de String → LocalDate ---
    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date);
    }
}
