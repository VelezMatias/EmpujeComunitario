package com.empuje.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/informes")
public class InformeVoluntariosController {

    @GetMapping("/ranking_voluntarios")
    public String rankingVoluntarios(HttpSession session) {
        // Verifica si hay inicio de sesion o si se trata de rol (PRESIDENTE o COORDINADOR)
        Object rol = session.getAttribute("rol");
        if (rol == null) return "redirect:/auth/login";
        String r = String.valueOf(rol);
        if (!("PRESIDENTE".equals(r) || "COORDINADOR".equals(r))) {
            return "redirect:/home";
        }
        return "informes/ranking_voluntarios"; // templates/informes/ranking_voluntarios.html
    }
}
