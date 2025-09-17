package com.empuje.grpc.admin;

import jakarta.servlet.http.HttpSession;
import com.empuje.grpc.ong.UserServiceGrpc;
import com.empuje.grpc.ong.Empty;
import com.empuje.grpc.ong.CreateUserRequest;
import com.empuje.grpc.ong.UpdateUserRequest;
import com.empuje.grpc.ong.DeactivateUserRequest;
import com.empuje.grpc.ong.AuthContext;
import com.empuje.grpc.ong.Role;
import com.empuje.grpc.ong.ListUsersResponse;
import com.empuje.grpc.ong.ApiResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {

    private final UserServiceGrpc.UserServiceBlockingStub users;

    public AdminUsersController(UserServiceGrpc.UserServiceBlockingStub users) {
        this.users = users;
    }

    /* ---------- Helpers ---------- */
    private boolean isPresidente(HttpSession s) {
        Object rol = s.getAttribute("rol");
        return rol != null && "PRESIDENTE".equals(rol.toString());
    }

    private AuthContext authFromSession(HttpSession s) {
        // actor_id opcional; usamos el userId en sesión si está
        int actorId = 0;
        Object uid = s.getAttribute("userId");
        if (uid instanceof Integer)
            actorId = (Integer) uid;
        if (uid instanceof String) {
            try {
                actorId = Integer.parseInt((String) uid);
            } catch (Exception ignore) {
            }
        }
        return AuthContext.newBuilder()
                .setActorId(actorId)
                .setActorRole(Role.PRESIDENTE) // Solo presidente gestiona
                .build();
    }

    private Role parseRole(String val) {
        if (val == null)
            return Role.VOLUNTARIO;
        try {
            return Role.valueOf(val);
        } catch (IllegalArgumentException e) {
            return Role.VOLUNTARIO;
        }
    }

    /* ---------- Listado ---------- */
    @GetMapping
    public String list(Model model, HttpSession session) {
        if (!isPresidente(session))
            return "redirect:/home";
        ListUsersResponse resp = users.listUsers(Empty.newBuilder().build());
        model.addAttribute("usuarios", resp.getUsersList());
        return "admin/users_list";
    }

    /* ---------- Alta ---------- */
    @GetMapping("/new")
    public String createForm(Model model, HttpSession session) {
        if (!isPresidente(session))
            return "redirect:/home";
        model.addAttribute("formMode", "create");
        return "admin/user_form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String username,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String rol, // PRESIDENTE/VOCAL/COORDINADOR/VOLUNTARIO
            Model model, HttpSession session) {
        if (!isPresidente(session))
            return "redirect:/home";

        if (!StringUtils.hasText(username) || !StringUtils.hasText(nombre) ||
                !StringUtils.hasText(apellido) || !StringUtils.hasText(email)) {
            model.addAttribute("error", "Usuario, nombre, apellido y email son obligatorios");
            model.addAttribute("formMode", "create");
            return "admin/user_form";
        }

        CreateUserRequest req = CreateUserRequest.newBuilder()
                .setAuth(authFromSession(session))
                .setUsername(username)
                .setNombre(nombre)
                .setApellido(apellido)
                .setTelefono(telefono == null ? "" : telefono)
                .setEmail(email)
                .setRol(parseRole(rol))
                .build();

        ApiResponse resp = users.createUser(req);
        if (!resp.getSuccess()) {
            model.addAttribute("error", resp.getMessage());
            model.addAttribute("formMode", "create");
            return "admin/user_form";
        }
        return "redirect:/admin/users";
    }

    /* ---------- Edición ---------- */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model, HttpSession session) {
        if (!isPresidente(session))
            return "redirect:/home";
        // Reusamos listado y filtramos (simple para no agregar otra RPC)
        var list = users.listUsers(Empty.newBuilder().build()).getUsersList();
        var opt = list.stream().filter(u -> u.getId() == id).findFirst();
        if (opt.isEmpty()) {
            model.addAttribute("error", "Usuario no encontrado");
            return "admin/users_list";
        }
        model.addAttribute("u", opt.get());
        model.addAttribute("formMode", "edit");
        return "admin/user_form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable int id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String rol,
            @RequestParam(defaultValue = "false") boolean activo,
            Model model, HttpSession session) {
        if (!isPresidente(session))
            return "redirect:/home";

        UpdateUserRequest req = UpdateUserRequest.newBuilder()
                .setAuth(authFromSession(session))
                .setId(id)
                .setNombre(nombre)
                .setApellido(apellido)
                .setTelefono(telefono == null ? "" : telefono)
                .setEmail(email)
                .setRol(parseRole(rol))
                .setActivo(activo)
                .build();

        ApiResponse resp = users.updateUser(req);
        if (!resp.getSuccess()) {
            model.addAttribute("error", resp.getMessage());
            model.addAttribute("formMode", "edit");
            // Intenta recargar el usuario para la vista
            var list = users.listUsers(Empty.newBuilder().build()).getUsersList();
            var opt = list.stream().filter(u -> u.getId() == id).findFirst();
            opt.ifPresent(u -> model.addAttribute("u", u));
            return "admin/user_form";
        }
        return "redirect:/admin/users";
    }

    /* ---------- Baja (inactivar) ---------- */
    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable int id, HttpSession session) {
        if (!isPresidente(session))
            return "redirect:/home";
        DeactivateUserRequest req = DeactivateUserRequest.newBuilder()
                .setAuth(authFromSession(session))
                .setId(id)
                .build();
        users.deactivateUser(req);
        return "redirect:/admin/users";
    }
}
