package se.iths.oscarp.microprojectauthserver.dto;

public record AuthResult(
        String accessToken,
        TokenResponseDTO response
) {
}
