package com.empuje.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InformeVoluntariosController {

    @GetMapping("/informes/ranking-voluntarios")
    public String rankingVoluntarios() {
        return "informes/ranking_voluntarios";
    }
}
