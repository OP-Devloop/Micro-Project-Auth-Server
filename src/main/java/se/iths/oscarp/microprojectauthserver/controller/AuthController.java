package se.iths.oscarp.microprojectauthserver.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.iths.oscarp.microprojectauthserver.dto.AuthResult;
import se.iths.oscarp.microprojectauthserver.dto.LoginRequestDTO;
import se.iths.oscarp.microprojectauthserver.dto.TokenResponseDTO;
import se.iths.oscarp.microprojectauthserver.service.AuthService;

import java.time.Duration;
import java.util.Map;

// Handles authentication-related endpoints
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokenResponseDTO login(
            @RequestBody LoginRequestDTO request,
            HttpServletResponse response
    ) {
        AuthResult result = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from(
                        "accessToken",
                        result.accessToken()
                )
                .httpOnly(true)
                .secure(false) // true in production with HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(60))
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        return result.response();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false) // true in production with HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        return ResponseEntity.noContent().build();
    }

    // Exposes public JWK set for JWT verification
    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> publicJwks() {
        return ResponseEntity.ok(authService.publicJwkSet());
    }
}
