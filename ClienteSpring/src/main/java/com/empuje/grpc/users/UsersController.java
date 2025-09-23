package com.empuje.grpc.users;

import io.grpc.StatusRuntimeException;
import com.empuje.grpc.ong.UserServiceGrpc;
import com.empuje.grpc.ong.LoginRequest;
import com.empuje.grpc.ong.ListUsersResponse;
import com.empuje.grpc.ong.Empty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserServiceGrpc.UserServiceBlockingStub users;

    public UsersController(UserServiceGrpc.UserServiceBlockingStub users) {
        this.users = users;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            LoginRequest req = LoginRequest.newBuilder()
                    .setUsernameOrEmail(body.getOrDefault("identifier", ""))
                    .setPassword(body.getOrDefault("password", ""))
                    .build();
            var resp = users.login(req);
            return ResponseEntity.ok(Map.of(
                    "ok", resp.getSuccess(),
                    "mensaje", resp.getMessage(),
                    "userId", resp.getUserId(),
                    "rol", resp.getRol().name()));
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(502).body(Map.of("ok", false, "mensaje", e.getStatus().getDescription()));
        }
    }

    @GetMapping
    public ResponseEntity<?> list() {
        try {
            ListUsersResponse resp = users.listUsers(Empty.newBuilder().build());
            return ResponseEntity.ok(resp.getUsersList());
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(502).body(Map.of("ok", false, "mensaje", e.getStatus().getDescription()));
        }
    }
}
