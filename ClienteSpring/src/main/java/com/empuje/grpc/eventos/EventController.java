package com.empuje.grpc.eventos;

import com.empuje.grpc.web.EventGateway; 
import jakarta.servlet.http.HttpSession;
import ong.ListEventsResponse;
import ong.Role;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Controller
@RequestMapping("/eventos")
public class EventController {

    private final EventGateway gateway;

    public EventController(EventGateway gateway) {
        this.gateway = gateway;
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


    
    

}
