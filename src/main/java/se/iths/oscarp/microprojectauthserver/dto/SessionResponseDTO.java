package se.iths.oscarp.microprojectauthserver.dto;

import java.util.List;

public record SessionResponseDTO(
        long expiresIn,
        String subject,
        List<String> roles
) {
}
