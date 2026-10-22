package se.iths.oscarp.microprojectauthserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.iths.oscarp.microprojectauthserver.dto.AuthResult;
import se.iths.oscarp.microprojectauthserver.dto.LoginRequestDTO;
import se.iths.oscarp.microprojectauthserver.dto.SessionResponseDTO;
import se.iths.oscarp.microprojectauthserver.service.AuthService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // Injects the service responsible for authentication operations.
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequestDTO request
    ) {
        // Authenticates the user and retrieves the token and response data.
        AuthResult result = authService.login(request);

        // Builds the JSON response containing the access token and user details.
        Map<String, Object> body = Map.of(
                "accessToken", result.accessToken(),
                "expiresIn", result.response().expiresIn(),
                "subject", result.response().subject(),
                "roles", result.response().roles()
        );

        return ResponseEntity.ok(body);
    }

    // Exposes the public JWK set for JWT signature verification.
    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> publicJwks() {
        return ResponseEntity.ok(authService.publicJwkSet());
    }

    @GetMapping("/me")
    public ResponseEntity<SessionResponseDTO> me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        // Retrieves the user's roles from the authenticated JWT.
        List<String> roles = jwt.getClaimAsStringList("roles");

        // Builds a session response using the token's expiration and user details.
        SessionResponseDTO response = new SessionResponseDTO(
                remainingSeconds(jwt),
                jwt.getSubject(),
                roles == null ? List.of() : roles
        );

        return ResponseEntity.ok(response);
    }

    // Calculates how many seconds remain before the JWT expires.
    private long remainingSeconds(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();

        // Returns zero if the token has no expiration timestamp.
        if (expiresAt == null) {
            return 0;
        }

        // Ensures the remaining time never becomes negative.
        return Math.max(
                0,
                Duration.between(
                        Instant.now(),
                        expiresAt
                ).getSeconds()
        );
    }
}
