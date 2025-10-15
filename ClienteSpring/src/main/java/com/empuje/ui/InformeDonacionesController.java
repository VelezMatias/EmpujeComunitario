package com.empuje.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InformeDonacionesController {

    @GetMapping("/informes/donaciones")
    public String donaciones() {
        return "informes/donaciones";
    }
}
