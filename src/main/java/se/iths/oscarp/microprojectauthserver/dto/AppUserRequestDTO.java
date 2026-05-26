package se.iths.oscarp.microprojectauthserver.dto;

import jakarta.validation.constraints.NotBlank;

public record AppUserRequestDTO(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password
) {
}