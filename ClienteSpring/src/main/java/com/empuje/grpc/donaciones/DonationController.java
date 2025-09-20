package com.empuje.grpc.donaciones;

import com.empuje.grpc.web.DonationGateway;
import jakarta.servlet.http.HttpSession;
import ong.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/donaciones")
public class DonationController {

    private final DonationGateway gateway;

    public DonationController(DonationGateway gateway) {
        this.gateway = gateway;
    }

    private Role role(HttpSession s) {
        Object r = s.getAttribute("rol");
        if (r instanceof Role) return (Role) r;
        if (r instanceof String) { try { return Role.valueOf((String) r); } catch (Exception ignore) {} }
        if (r instanceof Integer) {
            Role mapped = Role.forNumber((Integer) r);
            if (mapped != null) return mapped;
        }
        return Role.VOLUNTARIO;
    }

    private boolean canManage(HttpSession s) {
        Role r = role(s);
        return r == Role.PRESIDENTE || r == Role.VOCAL;
    }

    private int userId(HttpSession s) {
        Object v = s.getAttribute("userId");
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Long) return ((Long) v).intValue();
        if (v instanceof String) { try { return Integer.parseInt((String) v); } catch (Exception ignore) {} }
        return 0;
    }

    // LIST
    @GetMapping
    public String list(HttpSession s, Model model) {
        if (!canManage(s)) return "redirect:/home";
        List<DonationItem> items = gateway.list().getItemsList();
        model.addAttribute("items", items);
        return "donaciones/list";
    }

    // (Las acciones de alta/mod/ baja se agregan en commits siguientes)
}
