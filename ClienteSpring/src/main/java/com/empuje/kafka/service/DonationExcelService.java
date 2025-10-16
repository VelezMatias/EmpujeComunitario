package com.empuje.service;

import com.empuje.repo.JdbcDonationReportRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DonationExcelService {

    private final JdbcDonationReportRepository repo;

    public DonationExcelService(JdbcDonationReportRepository repo) {
        this.repo = repo;
    }

    public byte[] generarExcel(String categoriaFiltro, LocalDate from, LocalDate to, String eliminado)
            throws Exception {
        // 1) Traer categorías que matchean filtros
        List<String> categorias = repo.findCategoriasFiltradas(categoriaFiltro, from, to, eliminado);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // === Estilos ===
            CellStyle headerStyle = wb.createCellStyle();
            Font bold = wb.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            CellStyle dateStyle = wb.createCellStyle();
            short df = wb.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd");
            dateStyle.setDataFormat(df);

            // 2) Por cada categoría, crear una hoja con el detalle SIN sumarizar
            for (String cat : categorias) {
                String sheetName = sanitizeSheetName(cat);
                Sheet sh = wb.createSheet(sheetName);

                // Encabezados
                Row h = sh.createRow(0);
                String[] cols = { "Fecha de Alta", "Descripción", "Cantidad", "Eliminado", "Usuario Alta" };
                for (int i = 0; i < cols.length; i++) {
                    Cell c = h.createCell(i);
                    c.setCellValue(cols[i]);
                    c.setCellStyle(headerStyle);
                }

                // Datos
                List<Map<String, Object>> rows = repo.findDetallesParaExcel(cat, from, to, eliminado);

                int r = 1;
                for (Map<String, Object> row : rows) {
                    Row excelRow = sh.createRow(r++);

                    // === Fecha Alta ===
                    Cell c0 = excelRow.createCell(0);
                    Object fechaObj = row.get("fecha_alta");
                    if (fechaObj instanceof java.sql.Date d) {
                        c0.setCellValue(d.toLocalDate());
                        c0.setCellStyle(dateStyle);
                    } else if (fechaObj instanceof java.sql.Timestamp ts) {
                        c0.setCellValue(ts.toLocalDateTime().toLocalDate());
                        c0.setCellStyle(dateStyle);
                    } else if (fechaObj instanceof java.util.Date ud) {
                        c0.setCellValue(new java.sql.Date(ud.getTime()).toLocalDate());
                        c0.setCellStyle(dateStyle);
                    } else if (fechaObj != null) {
                        c0.setCellValue(String.valueOf(fechaObj));
                    }

                    // === Descripción ===
                    excelRow.createCell(1).setCellValue(safeString(row.get("descripcion")));

                    // === Cantidad ===
                    excelRow.createCell(2).setCellValue(safeNumber(row.get("cantidad")));

                    // === Eliminado (0/1 -> NO/SI) ===
                    excelRow.createCell(3).setCellValue(toSiNo(row.get("eliminado")));

                    // === Usuario Alta (nombre del usuario) ===
                    String usuarioAlta = safeString(row.get("usuario_alta"));
                    excelRow.createCell(4).setCellValue(usuarioAlta.isEmpty() ? "N/A" : usuarioAlta);
                }

                // Autosize columnas
                for (int i = 0; i < cols.length; i++) {
                    sh.autoSizeColumn(i);
                }
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }


    private static String sanitizeSheetName(String name) {
        if (name == null || name.isBlank())
            return "Hoja";
        // Excel: no permite []:*?/\
        String cleaned = name.replaceAll("[\\[\\]\\:\\*\\?/\\\\]", " ");
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }

    private static String safeString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static double safeNumber(Object o) {
        if (o == null)
            return 0d;
        if (o instanceof Number n)
            return n.doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return 0d;
        }
    }

    private static String toSiNo(Object val) {
        if (val == null)
            return "";
        if (val instanceof Number n)
            return n.intValue() == 1 ? "SI" : "NO";
        String s = val.toString().trim().toUpperCase();
        if ("1".equals(s))
            return "SI";
        if ("0".equals(s))
            return "NO";
        return s;
    }
}
