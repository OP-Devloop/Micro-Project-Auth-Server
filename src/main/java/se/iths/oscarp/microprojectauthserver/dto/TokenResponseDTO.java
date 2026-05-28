package se.iths.oscarp.microprojectauthserver.dto;

import java.util.List;

// DTO for JWT authentication response
public record TokenResponseDTO(

        // Generated JWT access token
        String accessToken,

        // Token expiration time in seconds
        long expiresIn,

        // Authenticated username/subject
        String subject,

        // User roles included in the token
        List<String> roles
) {
}
