package com.empuje.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InformeEventosController {

    @GetMapping("/informes/eventos-participacion")
    public String eventos() {
        return "informes/eventos_participacion";
    }
}
