package com.empuje.grpc.eventos;

import com.empuje.grpc.web.EventGateway; 
import jakarta.servlet.http.HttpSession;
import ong.ListEventsResponse;
import ong.Role;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ong.ApiResponse;
import ong.AssignDonationToEventRequest;
import ong.RemoveDonationFromEventRequest;
import ong.ListDonationsByEventRequest;
import ong.ListDonationsByEventResponse;
import ong.EventDonationLink;

import java.time.LocalDateTime;
import java.time.ZoneId;
<<<<<<< Updated upstream
=======
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.List;

>>>>>>> Stashed changes


@Controller
@RequestMapping("/eventos")
public class EventController {

    private final EventGateway gateway;
<<<<<<< Updated upstream

    public EventController(EventGateway gateway) {
        this.gateway = gateway;
=======
    private final UserServiceGrpc.UserServiceBlockingStub users;
    private final DonationServiceGrpc.DonationServiceBlockingStub donations; // <-- NUEVO

    public EventController(EventGateway gateway, UserServiceGrpc.UserServiceBlockingStub users, DonationServiceGrpc.DonationServiceBlockingStub donations ) {
        this.gateway = gateway;
        this.users = users;
        this.donations = donations;
>>>>>>> Stashed changes
    }

   
    private Role role(HttpSession s) {
    Object r = s.getAttribute("rol");
    if (r instanceof Role) return (Role) r;                 // enum correcto
    if (r instanceof String) {                              // "PRESIDENTE"
        try { return Role.valueOf((String) r); } catch (IllegalArgumentException ignored) {}
    }
    if (r instanceof Integer) {                             // 1,2,3,4
        Role mapped = Role.forNumber((Integer) r);
        if (mapped != null) return mapped;
    }
    return Role.VOLUNTARIO;
    }




    private int userId(HttpSession s) {
        Object v = s.getAttribute("userId");
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Long) return ((Long) v).intValue();
        return 0;
    }
    private boolean canManage(HttpSession s) {
        Role r = role(s);
        return r == Role.COORDINADOR || r == Role.PRESIDENTE;
    }

    @GetMapping
public String list(Model model) {
    try {
        var res = gateway.listAll();
        model.addAttribute("events", res.getEventsList());
        model.addAttribute("eventsCount", res.getEventsCount());
    } catch (io.grpc.StatusRuntimeException sre) {
        model.addAttribute("error", "gRPC: " + sre.getStatus() + " - " + sre.getStatus().getDescription());
        model.addAttribute("events", java.util.List.of());
        model.addAttribute("eventsCount", 0);
    } catch (Exception ex) {
        model.addAttribute("error", "Error listando eventos: " + ex.getMessage());
        model.addAttribute("events", java.util.List.of());
        model.addAttribute("eventsCount", 0);
    }
    return "events/list";
}

    @GetMapping("/new")
    public String newForm(HttpSession s, Model model) {
        if (!canManage(s)) return "redirect:/eventos";
        model.addAttribute("nowLocal", LocalDateTime.now());
        return "events/new_event"; // form con: nombre, descripcion, fechaLocal (datetime-local)
    }

    @PostMapping("/new")
    public String create(
            HttpSession s, Model model,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String fechaLocal
    ) {
        if (!canManage(s)) return "redirect:/eventos";
        try {
            LocalDateTime ldt = LocalDateTime.parse(fechaLocal);
            if (!ldt.isAfter(LocalDateTime.now())) {
                model.addAttribute("error", "La fecha/hora debe ser a futuro.");
                return "events/new_event";
            }
            ZoneId zona = ZoneId.systemDefault();
            var resp = gateway.createFromLocal(nombre.trim(), descripcion, ldt, zona, userId(s), role(s));
            if (!resp.getSuccess()) {
                model.addAttribute("error", resp.getMessage());
                return "events/new_event";
            }
            return "redirect:/eventos";
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                return "events/new_event";
            }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, HttpSession s, Model model) {
        if (!canManage(s)) return "redirect:/eventos";
        var list = gateway.listAll().getEventsList();
        var ev = list.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
        if (ev == null) return "redirect:/eventos";
        model.addAttribute("ev", ev);
        return "events/edit"; // form con: nombre, descripcion, fechaLocal
    }

<<<<<<< Updated upstream
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable int id, HttpSession s, Model model,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String fechaLocal
    ) {
        if (!canManage(s)) return "redirect:/eventos";
        try {
            LocalDateTime ldt = LocalDateTime.parse(fechaLocal);
            if (!ldt.isAfter(LocalDateTime.now())) {
                model.addAttribute("error", "La fecha/hora debe ser a futuro.");
                return "events/edit";
            }
            ZoneId zona = ZoneId.systemDefault();
            gateway.updateFromLocal(id, nombre.trim(), descripcion, ldt, zona, userId(s), role(s));
            return "redirect:/eventos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "events/edit";
=======
    if (q != null && !q.isBlank()) {
        String qLower = q.toLowerCase();
        allUsers = allUsers.stream().filter(u ->
                (u.getNombre() != null && u.getNombre().toLowerCase().contains(qLower)) ||
                (u.getApellido() != null && u.getApellido().toLowerCase().contains(qLower))
        ).collect(java.util.stream.Collectors.toList());
    }

    java.util.Set<Integer> memberIds = ev.getMiembrosList().stream()
            .collect(java.util.stream.Collectors.toSet());

    java.util.List<ong.User> currentParticipants = usersAllRaw.stream()
            .filter(u -> memberIds.contains(u.getId()))
            .collect(java.util.stream.Collectors.toList());

    // --- donaciones planificadas del evento ---
    var planned = gateway.listDonationsByEvent(id);
    model.addAttribute("plannedDonations", planned);

    // --- catálogo de donaciones + mapa id->donation ---
    java.util.List<ong.DonationItem> allDonations;
    try {
        allDonations = donations.listDonationItems(ong.Empty.getDefaultInstance()).getItemsList();
    } catch (Exception e) {
        allDonations = java.util.List.of();
    }
    var donationsById = allDonations.stream()
            .collect(Collectors.toMap(ong.DonationItem::getId, Function.identity()));
    model.addAttribute("allDonations", allDonations);
    model.addAttribute("donationsById", donationsById);

    // --- atributos existentes ---
    model.addAttribute("ev", ev);
    model.addAttribute("fechaLocalStr", fechaLocalStr);
    model.addAttribute("users", allUsers);
    model.addAttribute("memberIds", memberIds);
    model.addAttribute("q", q == null ? "" : q);
    model.addAttribute("currentParticipants", currentParticipants);

    return "events/edit_event"; // ojo con el nombre de la vista
}



   @PostMapping("/{id}/edit")
public String update(@PathVariable int id,
                     HttpSession s, Model model,
                     @RequestParam String nombre,
                     @RequestParam(required = false) String descripcion,
                     @RequestParam String fechaLocal) {
    if (!canManage(s)) return "redirect:/eventos";
    try {
        var ldt = LocalDateTime.parse(fechaLocal);
        if (!ldt.isAfter(LocalDateTime.now())) {
            model.addAttribute("error", "La fecha/hora debe ser a futuro.");
            // repoblar el modelo para re-renderizar la misma vista
            return editForm(id, null, s, model);
>>>>>>> Stashed changes
        }

        var zona = ZoneId.systemDefault();
        var resp = gateway.updateFromLocal(id, nombre.trim(), descripcion, ldt, zona, userId(s), role(s));
        if (!resp.getSuccess()) {
            model.addAttribute("error", resp.getMessage());
            return editForm(id, null, s, model);
        }
        return "redirect:/eventos/" + id + "/edit";
    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        return editForm(id, null, s, model);
    }
}


    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, HttpSession s) {
        if (!canManage(s)) return "redirect:/eventos";
        gateway.delete(id, userId(s), role(s));
        return "redirect:/eventos";
    }


    @GetMapping("/debug")
@ResponseBody
public String debug() {
    try {
        System.out.println("[Controller] /eventos/debug → gateway.listAll()");
        var res = gateway.listAll();
        StringBuilder sb = new StringBuilder();
        sb.append("OK | count=").append(res.getEventsCount());
        if (res.getEventsCount() > 0) {
            var e = res.getEventsList().get(0);
            sb.append(" | first={id=").append(e.getId())
              .append(", nombre=").append(e.getNombre())
              .append(", fecha=").append(e.getFechaHora())
              .append("}");
        }
        return sb.toString();
    } catch (io.grpc.StatusRuntimeException sre) {
        // Error de gRPC (server no reachable, UNIMPLEMENTED, INTERNAL, etc.)
        sre.printStackTrace();
        return "GRPC_ERROR | status=" + sre.getStatus()
             + " | desc=" + sre.getStatus().getDescription();
    } catch (Throwable t) {
        t.printStackTrace();
        return "APP_ERROR | " + t.getClass().getSimpleName() + " | " + t.getMessage();
    }
}



    @PostMapping("/{id}/donaciones/asignar")
    public String asignarDonacion(@PathVariable int id,
                                @RequestParam int donationId,
                                @RequestParam int cantidad,
                                HttpSession s, RedirectAttributes ra) {
        if (!canManage(s)) return "redirect:/eventos";
        var r = gateway.assignDonation(userId(s), role(s), id, donationId, cantidad);
        ra.addFlashAttribute(r.getSuccess() ? "ok" : "error", r.getMessage());
        return "redirect:/eventos/" + id + "/edit";
    }

    @PostMapping("/{id}/donaciones/quitar")
    public String quitarDonacion(@PathVariable int id,
                                @RequestParam int donationId,
                                HttpSession s, RedirectAttributes ra) {
        if (!canManage(s)) return "redirect:/eventos";
        var r = gateway.removeDonation(userId(s), role(s), id, donationId);
        ra.addFlashAttribute(r.getSuccess() ? "ok" : "error", r.getMessage());
        return "redirect:/eventos/" + id + "/edit";
    }


    
    

}
