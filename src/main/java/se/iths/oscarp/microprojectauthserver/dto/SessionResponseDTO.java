package se.iths.oscarp.microprojectauthserver.dto;

import java.util.List;

// Represents the authenticated user's session details.
public record SessionResponseDTO(
        long expiresIn,       // Remaining token lifetime in seconds.
        String subject,       // Username or identifier of the authenticated user.
        List<String> roles    // Roles assigned to the user.
) {
}