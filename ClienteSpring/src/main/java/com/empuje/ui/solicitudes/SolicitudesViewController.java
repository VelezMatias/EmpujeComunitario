package com.empuje.ui.solicitudes;

import com.empuje.kafka.entity.SolicitudExterna;
import com.empuje.kafka.entity.SolicitudItem;
import com.empuje.kafka.repo.SolicitudExternaRepo;
import com.empuje.kafka.repo.SolicitudItemRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; // <-- IMPORT CLAVE
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ui/solicitudes")
public class SolicitudesViewController {

    private final SolicitudExternaRepo solicitudes;
    private final SolicitudItemRepo items;

    public SolicitudesViewController(SolicitudExternaRepo solicitudes, SolicitudItemRepo items) {
        this.solicitudes = solicitudes;
        this.items = items;
    }

    // Listado paginado con ítems incluidos
    @GetMapping
    public String listado(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<SolicitudExterna> p = solicitudes.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));

        Map<String, List<SolicitudItem>> itemsBySolicitudId = new LinkedHashMap<>();
        for (SolicitudExterna se : p.getContent()) {
            String sid = se.getSolicitudId();
            itemsBySolicitudId.put(sid, items.findBySolicitudId(sid));
        }

        model.addAttribute("page", p);
        model.addAttribute("itemsBy", itemsBySolicitudId);
        return "solicitudes/list";
    }

    // Detalle de una solicitud por su solicitud_id
    @GetMapping("/{solicitudId}")
    public String detalle(@PathVariable String solicitudId, Model model) {
        var se = solicitudes.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la solicitud: " + solicitudId));
        var its = items.findBySolicitudId(solicitudId);

        model.addAttribute("solicitud", se);
        model.addAttribute("items", its);
        return "solicitudes/detalle";
    }

    // Baja lógica + cleanup de ítems
    @PostMapping("/{solicitudId}/eliminar")
    @Transactional
    public String eliminar(@PathVariable String solicitudId) {
        var se = solicitudes.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la solicitud: " + solicitudId));

        se.setEstado("ANULADA");
        solicitudes.save(se);

        items.deleteBySolicitudId(solicitudId);

        return "redirect:/ui/solicitudes?msg=Solicitud%20" + solicitudId + "%20anulada";
    }
}
