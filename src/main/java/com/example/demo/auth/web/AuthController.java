package com.example.demo.auth.web;

import java.util.Map;

import com.example.demo.auth.entity.AccountUser;
import com.example.demo.auth.entity.AccountUserRepository;
import com.example.demo.auth.jwt.JwtService;
import com.example.demo.common.BaseController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController implements BaseController {

    private final AccountUserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AccountUserRepository repo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public record LoginRequest(String username, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req == null || req.username() == null || req.password() == null
            || req.username().isBlank() || req.password().isBlank()) {
            return ResponseEntity.badRequest().body(fail("username/password 不能为空"));
        }

        AccountUser user = repo.findByUsernameAndIsDeletedFalse(req.username())
            .orElse(null);
        if (user == null || !AccountUser.STATUS_ACTIVE.equals(user.getStatus())) {
            return ResponseEntity.status(401).body(fail("用户名或密码错误"));
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(fail("用户名或密码错误"));
        }

        String token = jwtService.issueToken(user.getUsername());
        Map<String, Object> data = Map.of(
            "token", token,
            "token_type", "Bearer",
            "expires_in", jwtService.ttlSeconds()
        );
        return ResponseEntity.ok(success(data));
    }
}
