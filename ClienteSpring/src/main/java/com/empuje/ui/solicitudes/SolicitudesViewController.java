package com.empuje.ui.solicitudes;

import com.empuje.kafka.dto.SolicitudDtos;
import com.empuje.kafka.entity.SolicitudExterna;
import com.empuje.kafka.entity.SolicitudItem;
import com.empuje.kafka.repo.SolicitudCumplidaRepo;
import com.empuje.kafka.repo.SolicitudExternaRepo;
import com.empuje.kafka.repo.SolicitudItemRepo;
import com.empuje.kafka.repo.SolicitudPropItemRepository;
import com.empuje.kafka.repo.SolicitudRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ui/solicitudes")
public class SolicitudesViewController {

    private final SolicitudExternaRepo solicitudes;
    private final SolicitudItemRepo items;

    // --- PROPIAS ---
    private final SolicitudRepository solicitudRepository;
    private final SolicitudPropItemRepository solicitudPropItemRepository;

    // --- CUMPLIDAS (propias) ---
    private final SolicitudCumplidaRepo cumplidasRepo;

    private final ObjectMapper om = new ObjectMapper();

    public SolicitudesViewController(SolicitudExternaRepo solicitudes,
                                     SolicitudItemRepo items,
                                     SolicitudRepository solicitudRepository,
                                     SolicitudPropItemRepository solicitudPropItemRepository,
                                     SolicitudCumplidaRepo cumplidasRepo) {
        this.solicitudes = solicitudes;
        this.items = items;
        this.solicitudRepository = solicitudRepository;
        this.solicitudPropItemRepository = solicitudPropItemRepository;
        this.cumplidasRepo = cumplidasRepo;
    }

    @GetMapping
    public String listado(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {

        // ===== IZQUIERDA: EXTERNAS =====
        Page<SolicitudExterna> p = solicitudes.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));

        Map<String, List<SolicitudItem>> itemsBySolicitudId = new LinkedHashMap<>();
        for (SolicitudExterna se : p.getContent()) {
            String sid = se.getSolicitudId();
            itemsBySolicitudId.put(sid, items.findBySolicitudId(sid));
        }
        model.addAttribute("page", p);
        model.addAttribute("itemsBy", itemsBySolicitudId);

        // ===== DERECHA: PROPIAS =====
        var propias = solicitudRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaHora"));
        model.addAttribute("propias", propias); 

        // Ítems de PROPIAS: usando el JSON guardado
        Map<String, List<SolicitudDtos.Item>> propItemsBy = new LinkedHashMap<>();
        for (var sp : propias) {
            try {
                var dto = om.readValue(sp.getPayloadJson(), SolicitudDtos.SolicitudDonaciones.class);
                propItemsBy.put(sp.getSolicitudId(), dto.getItems() == null ? List.of() : dto.getItems());
            } catch (Exception e) {
                propItemsBy.put(sp.getSolicitudId(), List.of());
            }
        }
        model.addAttribute("propItemsBy", propItemsBy); 

        // Set de PROPIAS cumplidas (para pintar badge "CUMPLIDA")
        var cumplidasSet = cumplidasRepo.findAll().stream()
                .map(c -> c.getSolicitudId())
                .collect(Collectors.toSet());
        model.addAttribute("cumplidas", cumplidasSet);

        return "solicitudes/list";
    }
}
