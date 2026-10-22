package se.iths.oscarp.microprojectauthserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.iths.oscarp.microprojectauthserver.dto.AppUserRequestDTO;
import se.iths.oscarp.microprojectauthserver.mapper.AppUserMapper;
import se.iths.oscarp.microprojectauthserver.model.AppUser;
import se.iths.oscarp.microprojectauthserver.repository.AppUserRepository;

import java.time.LocalDateTime;
import java.util.List;

// Handles application user retrieval and creation.
@Service
@RequiredArgsConstructor
public class AppUserService {

    // Provides database access, entity mapping, and password hashing.
    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;

    // Retrieves all registered users.
    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    // Retrieves a user by ID, or returns null if not found.
    public AppUser findById(long id) {
        return appUserRepository.findById(id).orElse(null);
    }

    // Retrieves a user by username, throwing an exception if not found.
    public AppUser findByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Creates a user, hashes their password, and saves them to the database.
    public AppUser create(AppUserRequestDTO dto, String username) {
        // Maps the request DTO to a user entity.
        AppUser appUser = appUserMapper.toEntity(dto);

        // Hashes the password before storing it.
        appUser.setPassword(
                passwordEncoder.encode(appUser.getPassword())
        );

        // Assigns the default role and creator.
        appUser.setRole("USER");
        appUser.setCreatedBy("system");

        // Sets the date of birth to the current date.
        appUser.setDateOfBirth(LocalDateTime.now().toLocalDate());

        return appUserRepository.save(appUser);
    }
}
