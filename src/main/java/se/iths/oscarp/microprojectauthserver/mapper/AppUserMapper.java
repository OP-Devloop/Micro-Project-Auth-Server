package se.iths.oscarp.microprojectauthserver.mapper;

import org.mapstruct.Mapper;
import se.iths.oscarp.microprojectauthserver.dto.AppUserRequestDTO;
import se.iths.oscarp.microprojectauthserver.model.AppUser;

// Maps AppUser DTOs to entity objects
@Mapper(componentModel = "spring")
public interface AppUserMapper {
    AppUser toEntity(AppUserRequestDTO dto);
}