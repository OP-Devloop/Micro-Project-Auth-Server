package se.iths.oscarp.microprojectauthserver.dto;

// Holds the generated access token and its associated response details.
public record AuthResult(
        String accessToken,
        TokenResponseDTO response
) {
}
