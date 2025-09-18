package com.empuje.grpc.auth;

import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpSession;
import ong.UserServiceGrpc;
import ong.LoginRequest;
import ong.CreateUserRequest;
import ong.AuthContext;
import ong.Role;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserServiceGrpc.UserServiceBlockingStub users;

    public AuthController(UserServiceGrpc.UserServiceBlockingStub users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String identifier,
            @RequestParam String password,
            HttpSession session, Model model) {
        try {
            LoginRequest req = LoginRequest.newBuilder()
                    .setUsernameOrEmail(identifier)
                    .setPassword(password)
                    .build();

            var resp = users.login(req);

            if (!resp.getSuccess()) {
                model.addAttribute("error", resp.getMessage());
                return "login";
            }

            session.setAttribute("userId", resp.getUserId());
            session.setAttribute("username", identifier);
            session.setAttribute("rol", resp.getRol().name());
            return "redirect:/home";

        } catch (StatusRuntimeException e) {
            model.addAttribute("error", "Error de autenticación: " + e.getStatus().getDescription());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        return "redirect:/"; // más robusto que /home
    }

    @GetMapping("/register")
    public String registerForm(HttpSession session) {
        // Solo PRESIDENTE puede registrar
        if (session.getAttribute("rol") == null ||
                !"PRESIDENTE".equals(session.getAttribute("rol").toString())) {
            return "redirect:/home";
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String rol, // <--- NUEVO: viene del form
            Model model,
            HttpSession session) {
        // Bloqueo por rol
        if (session.getAttribute("rol") == null ||
                !"PRESIDENTE".equals(session.getAttribute("rol").toString())) {
            return "redirect:/home";
        }

        if (!StringUtils.hasText(username) || !StringUtils.hasText(email) ||
                !StringUtils.hasText(nombre) || !StringUtils.hasText(apellido)) {
            model.addAttribute("error", "Usuario, nombre, apellido y email son obligatorios");
            return "register";
        }

        try {
            int actorId = 0;
            Object uid = session.getAttribute("userId");
            if (uid instanceof Integer)
                actorId = (Integer) uid;
            else if (uid instanceof String) {
                try {
                    actorId = Integer.parseInt((String) uid);
                } catch (Exception ignored) {
                }
            }

            // Parsear el enum Role desde el string del form
            Role rolEnum;
            try {
                rolEnum = Role.valueOf(rol);
            } catch (IllegalArgumentException ex) {
                rolEnum = Role.VOLUNTARIO; // fallback seguro
            }

            var req = CreateUserRequest.newBuilder()
                    .setAuth(AuthContext.newBuilder()
                            .setActorId(actorId)
                            .setActorRole(Role.PRESIDENTE)
                            .build())
                    .setUsername(username)
                    .setNombre(nombre)
                    .setApellido(apellido)
                    .setTelefono(telefono == null ? "" : telefono)
                    .setEmail(email)
                    .setRol(rolEnum) // <--- usar el rol elegido
                    .build();

            var resp = users.createUser(req);

            if (!resp.getSuccess()) {
                model.addAttribute("error", resp.getMessage());
                return "register";
            }

            model.addAttribute("ok", "Usuario creado. Revisá la consola del servidor (contraseña generada).");
            return "register";

        } catch (StatusRuntimeException e) {
            model.addAttribute("error", "Error al registrar: " + e.getStatus().getDescription());
            return "register";
        }
    }


    @GetMapping("/eventos")
    public String eventosForm(HttpSession session) {
        // Solo PRESIDENTE y VOCAl
        if (session.getAttribute("rol") == null ||
                !"PRESIDENTE".equals(session.getAttribute("rol").toString())) {
            return "redirect:/home";
        }
        return "eventos";
    }



}
