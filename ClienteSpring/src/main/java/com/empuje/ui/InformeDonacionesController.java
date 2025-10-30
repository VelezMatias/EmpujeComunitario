package com.empuje.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InformeDonacionesController {

    /** INFORME /informes/donaciones -> templates/informes/donaciones.html */
    @GetMapping("/informes/donaciones")
    public String informeDonaciones(HttpSession session, Model model) {
        model.addAttribute("session", session); // si lo usás en el header/menu
        return "informes/donaciones";
    }
}
