package com.empuje.service;

import com.empuje.repo.JdbcEventReportRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EventExcelService {

    private final JdbcEventReportRepository repo;

    public EventExcelService(JdbcEventReportRepository repo) {
        this.repo = repo;
    }

    public byte[] generarExcel(Integer usuarioId, LocalDate from, LocalDate to, String reparto) throws Exception {
        List<Map<String, Object>> events = repo.findEventsForUser(usuarioId, from, to, reparto);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            CellStyle headerStyle = wb.createCellStyle();
            Font bold = wb.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            Sheet sh = wb.createSheet("Eventos");
            Row h = sh.createRow(0);
            String[] cols = { "Fecha", "Día", "Nombre evento", "Descripción", "Donación - Descripción", "Donación - Cantidad" };
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            int r = 1;
            for (Map<String, Object> ev : events) {
                Integer id = (Integer) ev.get("id");
                Object fechaObj = ev.get("fecha_hora");
                java.time.LocalDate fecha = null;
                java.time.LocalDateTime fdt = null;
                if (fechaObj instanceof java.sql.Timestamp) fdt = ((java.sql.Timestamp) fechaObj).toLocalDateTime();
                if (fdt != null) fecha = fdt.toLocalDate();

                List<Map<String, Object>> donations = repo.findDonationsForEvent(id);

                if (donations == null || donations.isEmpty()) {
                    Row row = sh.createRow(r++);
                    row.createCell(0).setCellValue(fecha == null ? "" : fecha.toString());
                    row.createCell(1).setCellValue(fecha == null ? "" : String.valueOf(fecha.getDayOfMonth()));
                    row.createCell(2).setCellValue(String.valueOf(ev.get("nombre")));
                    row.createCell(3).setCellValue(String.valueOf(ev.get("descripcion")));
                } else {
                    for (Map<String, Object> d : donations) {
                        Row row = sh.createRow(r++);
                        row.createCell(0).setCellValue(fecha == null ? "" : fecha.toString());
                        row.createCell(1).setCellValue(fecha == null ? "" : String.valueOf(fecha.getDayOfMonth()));
                        row.createCell(2).setCellValue(String.valueOf(ev.get("nombre")));
                        row.createCell(3).setCellValue(String.valueOf(ev.get("descripcion")));
                        row.createCell(4).setCellValue(String.valueOf(d.get("descripcion")));
                        row.createCell(5).setCellValue(((Number) d.getOrDefault("cantidad", 0)).doubleValue());
                    }
                }
            }

            for (int i = 0; i < cols.length; i++) sh.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        }
    }
}
