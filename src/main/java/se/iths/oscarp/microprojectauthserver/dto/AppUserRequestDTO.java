package se.iths.oscarp.microprojectauthserver.dto;

import jakarta.validation.constraints.NotBlank;

// DTO for user registration/login requests
public record AppUserRequestDTO(

        // Username must not be empty
        @NotBlank(message = "Username is required")
        String username,

        // Password must not be empty
        @NotBlank(message = "Password is required")
        String password
) {
}