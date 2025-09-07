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
            // En tu proto el campo es username_or_email -> setter se llama
            // setUsernameOrEmail
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
            session.setAttribute("username", identifier); // o cargar username real si lo expone tu LoginResponse
            session.setAttribute("rol", resp.getRol().name());
            return "redirect:/home";

        } catch (StatusRuntimeException e) {
            model.addAttribute("error", "Error de autenticación: " + e.getStatus().getDescription());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            Model model) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(email)) {
            model.addAttribute("error", "Usuario y email son obligatorios");
            return "register";
        }

        try {
            CreateUserRequest req = CreateUserRequest.newBuilder()
                    .setAuth(AuthContext.newBuilder()
                            .setActorId(1) // simular PRESIDENTE para probar; luego atalo a sesión/admin real
                            .setActorRole(Role.PRESIDENTE)
                            .build())
                    .setUsername(username)
                    .setNombre(nombre)
                    .setApellido(apellido)
                    .setTelefono(telefono == null ? "" : telefono)
                    .setEmail(email)
                    .setRol(Role.VOLUNTARIO)
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/home";
    }
}
