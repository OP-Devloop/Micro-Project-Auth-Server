package se.iths.oscarp.microprojectauthserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.iths.oscarp.microprojectauthserver.dto.AppUserRequestDTO;
import se.iths.oscarp.microprojectauthserver.model.AppUser;
import se.iths.oscarp.microprojectauthserver.service.AppUserService;

import java.util.List;

@RestController
@RequestMapping("/appusers")
@RequiredArgsConstructor
public class AppUserController {

    // Provides business logic for managing application users.
    private final AppUserService appUserService;

    // Retrieves all registered users.
    @GetMapping
    public ResponseEntity<List<AppUser>> findAll() {
        return ResponseEntity.ok(appUserService.findAll());
    }

    // Retrieves a user by their ID.
    @GetMapping("/{id}")
    public ResponseEntity<AppUser> findById(@PathVariable Long id) {
        return ResponseEntity.ok(appUserService.findById(id));
    }

    // Retrieves the currently authenticated user using their username.
    @GetMapping("/me")
    public ResponseEntity<AppUser> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();

        return ResponseEntity.ok(appUserService.findByUsername(username));
    }

    // Creates a new user and returns HTTP 201 Created.
    @PostMapping
    public ResponseEntity<AppUser> create(@RequestBody AppUserRequestDTO dto) {
        AppUser appUser = appUserService.create(dto, "system");
        return ResponseEntity.status(HttpStatus.CREATED).body(appUser);
    }
}
