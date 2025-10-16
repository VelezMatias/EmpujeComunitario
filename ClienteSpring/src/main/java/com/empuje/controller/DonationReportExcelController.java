package com.empuje.controller;

import com.empuje.service.DonationExcelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class DonationReportExcelController {

    private final DonationExcelService excelService;

    public DonationReportExcelController(DonationExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping("/api/informes/donaciones/excel")
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "AMBOS") String eliminado
    ) throws Exception {

        byte[] bytes = excelService.generarExcel(categoria, from, to, eliminado);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=informe_donaciones.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
