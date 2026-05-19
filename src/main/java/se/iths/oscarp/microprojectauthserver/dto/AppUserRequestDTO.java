package se.iths.oscarp.microprojectauthserver.dto;

import java.time.LocalDate;

public record AppUserRequestDTO(
        String username,
        LocalDate dateOfBirth
) {
}
