package se.iths.oscarp.microprojectauthserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.iths.oscarp.microprojectauthserver.dto.AppUserRequestDTO;
import se.iths.oscarp.microprojectauthserver.mapper.AppUserMapper;
import se.iths.oscarp.microprojectauthserver.model.AppUser;
import se.iths.oscarp.microprojectauthserver.repository.AppUserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;

    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    public AppUser findById(long id) {
        return appUserRepository.findById(id).orElse(null);
    }

    public AppUser create(AppUserRequestDTO dto, String username) {
        AppUser appUser = appUserMapper.toEntity(dto);
        appUser.setCreatedBy(username);
        appUser.setDateOfBirth(LocalDateTime.now().toLocalDate());
        return appUserRepository.save(appUser);
    }
}
