package se.iths.oscarp.microprojectauthserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.iths.oscarp.microprojectauthserver.dto.AppUserRequestDTO;
import se.iths.oscarp.microprojectauthserver.model.AppUser;
import se.iths.oscarp.microprojectauthserver.service.AppUserService;

import java.util.List;

@RestController
@RequestMapping("/appusers")
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;

    @GetMapping
    public ResponseEntity<List<AppUser>> findAll() {
        return ResponseEntity.ok(appUserService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUser> findById(@PathVariable Long id) {
        return ResponseEntity.ok(appUserService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AppUser> create(@RequestBody AppUserRequestDTO dto) {
        AppUser appUser = appUserService.create(dto, "system");
        return ResponseEntity.status(HttpStatus.CREATED).body(appUser);
    }
}
