package com.empuje.grpc.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({ "/", "/home" })
    public String home(Model model, HttpSession session) {
        model.addAttribute("mensaje", "Bienvenido");
        model.addAttribute("usuario", session.getAttribute("username"));
        model.addAttribute("rol", session.getAttribute("rol"));
        return "home";
    }
}
