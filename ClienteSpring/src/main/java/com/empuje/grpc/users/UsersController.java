package com.empuje.grpc.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import io.grpc.StatusRuntimeException;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private static final Logger log = LoggerFactory.getLogger(UsersController.class);

    private final UsersServiceGrpc.UsersServiceBlockingStub stub;

    public UsersController(UsersServiceGrpc.UsersServiceBlockingStub stub) {
        this.stub = stub;
    }

    @GetMapping("/health")
    public String health() { return "ok"; }

    @PostMapping(path = "/login",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> login(@RequestBody LoginDto body) {
        LoginRequest req = LoginRequest.newBuilder()
                .setUsername(body.getUsername() == null ? "" : body.getUsername())
                .setPassword(body.getPassword() == null ? "" : body.getPassword())
                .build();

        try {
            LoginResponse resp = stub.login(req);

            Map<String, Object> out = new HashMap<>();
            out.put("ok", resp.getOk());
            out.put("mensaje", resp.getMensaje());
            if (resp.hasUser()) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", resp.getUser().getId());
                u.put("username", resp.getUser().getUsername());
                u.put("nombre", resp.getUser().getNombre());
                u.put("apellido", resp.getUser().getApellido());
                u.put("email", resp.getUser().getEmail());
                u.put("telefono", resp.getUser().getTelefono());
                u.put("rol_id", resp.getUser().getRolId());
                u.put("activo", resp.getUser().getActivo());
                out.put("user", u);
            }
            return out;

        } catch (StatusRuntimeException e) {
            // Esto imprime exactamente el motivo: UNAVAILABLE, UNKNOWN, INTERNAL, etc.
            log.error("gRPC error calling UsersService.login: status={}, cause={}",
                    e.getStatus(), e.getCause() == null ? "-" : e.getCause().toString(), e);
            // Proyectamos un 502 para diferenciarlo de errores HTTP internos del gateway
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "gRPC error: " + e.getStatus().getCode() + " - " + e.getStatus().getDescription());
        } catch (Exception e) {
            log.error("Unexpected error in /api/users/login", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        }
    }
}
