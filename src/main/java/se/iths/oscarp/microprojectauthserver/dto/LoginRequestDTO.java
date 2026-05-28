package se.iths.oscarp.microprojectauthserver.dto;

import jakarta.validation.constraints.NotBlank;

// DTO for login requests
public record LoginRequestDTO(
        @NotBlank
        String username,
        @NotBlank
        String password
) {
}
