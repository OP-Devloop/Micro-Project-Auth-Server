package se.iths.oscarp.microprojectauthserver.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.iths.oscarp.microprojectauthserver.dto.LoginRequestDTO;
import se.iths.oscarp.microprojectauthserver.dto.TokenResponseDTO;
import se.iths.oscarp.microprojectauthserver.service.AuthService;

import java.util.Map;

// Handles authentication-related endpoints
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Authenticates user and returns JWT token
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Exposes public JWK set for JWT verification
    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> publicJwks() {
        return ResponseEntity.ok(authService.publicJwkSet());
    }
}
