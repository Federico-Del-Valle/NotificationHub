package com.example.notificationhub.auth;
import com.example.notificationhub.auth.dto.*;
import com.example.notificationhub.users.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registro y login con JWT")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid RegisterRequest req) {
        if (users.existsByUsername(req.username())) throw new IllegalArgumentException("username already exists");
        var user = User.builder()
                .username(req.username())
                .passwordHash(encoder.encode(req.password()))
                .rol(Rol.USER) // si tu enum se llama Rol
                .build();
        users.save(user);
        String token = jwt.generate(user.getUsername(), Map.of("role", user.getRol().name()));
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest req) {
        var user = users.findByUsername(req.username())
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        if (!encoder.matches(req.password(), user.getPasswordHash()))
            throw new IllegalArgumentException("invalid credentials");
        String token = jwt.generate(user.getUsername(), Map.of("role", user.getRol().name()));
        return new AuthResponse(token);
    }
}
