package se.iths.oscarp.microprojectauthserver.dto;

// DTO for returning user information
public record AppUserResponseDTO(
        Long id,
        String username
) {
}
