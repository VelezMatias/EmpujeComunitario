package com.empuje.grpc.eventos;

import com.empuje.grpc.web.EventGateway; 
import jakarta.servlet.http.HttpSession;
import ong.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/eventos")
public class EventController {

    private final EventGateway gateway;
    private final UserServiceGrpc.UserServiceBlockingStub users;

    public EventController(EventGateway gateway, UserServiceGrpc.UserServiceBlockingStub users ) {
        this.gateway = gateway;
        this.users = users;
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
        if (v instanceof String) try { return Integer.parseInt((String) v); } catch (Exception ignored) {}
        return 0;
    }
    private boolean canManage(HttpSession s) {
        Role r = role(s);
        return r == Role.COORDINADOR || r == Role.PRESIDENTE;
    }

    @GetMapping
    public String list(Model model) {
        var res = gateway.listAll();
        model.addAttribute("events", res.getEventsList());
        model.addAttribute("eventsCount", res.getEventsCount());
        return "events/list";
    }




    @GetMapping("/new")
    public String newForm(HttpSession s, Model model) {
        if (!canManage(s)) return "redirect:/eventos";
        model.addAttribute("nowLocal", LocalDateTime.now());
        return "events/new_event"; 
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
public String editForm(@PathVariable int id,
                       @RequestParam(name="q", required = false) String q,
                       HttpSession s, Model model) {
    if (!canManage(s)) return "redirect:/eventos";

    var list = gateway.listAll().getEventsList();
    var ev = list.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    if (ev == null) return "redirect:/eventos";

    // --- fechaLocalStr desde ev.fechaHora (ISO-8601 UTC) ---
    String fechaIso = ev.getFechaHora();
    String fechaLocalStr = "";
    try {
        java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(fechaIso);
        java.time.LocalDateTime ldtLocal = odt.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
        fechaLocalStr = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(ldtLocal);
    } catch (Exception ignore) {}

    // --- cargar usuarios ---
    java.util.List<ong.User> usersAllRaw;
    java.util.List<ong.User> allUsers;
    try {
        var usersResp = users.listUsers(ong.Empty.getDefaultInstance());
        usersAllRaw = new java.util.ArrayList<>(usersResp.getUsersList());
        allUsers    = new java.util.ArrayList<>(usersResp.getUsersList());
    } catch (Exception ex) {
        usersAllRaw = new java.util.ArrayList<>();
        allUsers    = new java.util.ArrayList<>();
    }

    if (q != null && !q.isBlank()) {
        String qLower = q.toLowerCase();
        allUsers = allUsers.stream().filter(u ->
                (u.getNombre() != null && u.getNombre().toLowerCase().contains(qLower)) ||
                (u.getApellido() != null && u.getApellido().toLowerCase().contains(qLower))
        ).collect(java.util.stream.Collectors.toList());
    }

    // ⬇️ ESTE BLOQUE NUEVO VA AQUÍ
    java.util.Set<Integer> memberIds = ev.getMiembrosList().stream()
            .collect(java.util.stream.Collectors.toSet());

    java.util.List<ong.User> currentParticipants = usersAllRaw.stream()
            .filter(u -> memberIds.contains(u.getId()))
            .collect(java.util.stream.Collectors.toList());
    // ⬆️

    model.addAttribute("ev", ev);
    model.addAttribute("fechaLocalStr", fechaLocalStr);
    model.addAttribute("users", allUsers); // para la tabla de búsqueda/Agregar
    model.addAttribute("memberIds", memberIds);
    model.addAttribute("q", q == null ? "" : q);
    model.addAttribute("currentParticipants", currentParticipants); // para la tabla de actuales

    return "events/edit_event";
}



    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable int id, HttpSession s, Model model,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String fechaLocal
    ) {
        if (!canManage(s)) return "redirect:/eventos";
        try {
            var ldt = LocalDateTime.parse(fechaLocal);
            if (!ldt.isAfter(LocalDateTime.now())) {
                model.addAttribute("error", "La fecha/hora debe ser a futuro.");
                return "events/edit";
            }
            var zona = ZoneId.systemDefault();
            var resp = gateway.updateFromLocal(id, nombre.trim(), descripcion, ldt, zona, userId(s), role(s));
            if (!resp.getSuccess()) {
                model.addAttribute("error", resp.getMessage());
                return "events/edit";
            }
            return "redirect:/eventos/" + id + "/edit";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "events/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, HttpSession s) {
        if (!canManage(s)) return "redirect:/eventos";
        gateway.delete(id, userId(s), role(s));
        return "redirect:/eventos";
    }

    // ---- gestionar miembros ----
    @PostMapping("/{id}/assign")
    public String assign(@PathVariable int id,
                         @RequestParam int userId,
                         @RequestParam(name="q", required = false) String q,
                         HttpSession s) {
        if (!canManage(s)) return "redirect:/eventos";
        gateway.assignMember(id, userId, userId(s), role(s));
        return "redirect:/eventos/" + id + "/edit" + (q != null && !q.isBlank() ? ("?q=" + q) : "");
    }

    @PostMapping("/{id}/remove")
    public String remove(@PathVariable int id,
                         @RequestParam int userId,
                         @RequestParam(name="q", required = false) String q,
                         HttpSession s) {
        if (!canManage(s)) return "redirect:/eventos";
        gateway.removeMember(id, userId, userId(s), role(s));
        return "redirect:/eventos/" + id + "/edit" + (q != null && !q.isBlank() ? ("?q=" + q) : "");
    }




    @GetMapping("/debug")
    @ResponseBody
    public String debug() {
        try {
            var res = gateway.listAll();
            StringBuilder sb = new StringBuilder("OK | count=").append(res.getEventsCount());
            if (res.getEventsCount() > 0) {
                var e = res.getEventsList().get(0);
                sb.append(" | first={id=").append(e.getId())
                .append(", nombre=").append(e.getNombre())
                .append(", fecha=").append(e.getFechaHora())
                .append(", miembros=").append(e.getMiembrosCount())
                .append("}");
            }
            return sb.toString();
        } catch (io.grpc.StatusRuntimeException sre) {
            return "GRPC_ERROR | " + sre.getStatus() + " | " + sre.getStatus().getDescription();
        } catch (Throwable t) {
            return "APP_ERROR | " + t.getClass().getSimpleName() + " | " + t.getMessage();
        }
    }


    
    

}
