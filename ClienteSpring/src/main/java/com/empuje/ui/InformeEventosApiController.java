package com.empuje.ui;

import com.empuje.service.EventExcelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class InformeEventosApiController {

    private final EventExcelService excelService;

    public InformeEventosApiController(EventExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping(path = "/api/informes/eventos/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam Integer usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "AMBOS") String reparto
    ) throws Exception {

        byte[] data = excelService.generarExcel(usuarioId, from, to, reparto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=eventos_participacion.xlsx");

        return ResponseEntity.ok().headers(headers).body(data);
    }
}
