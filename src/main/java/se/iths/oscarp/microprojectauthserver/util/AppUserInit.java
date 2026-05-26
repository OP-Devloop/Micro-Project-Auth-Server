package se.iths.oscarp.microprojectauthserver.util;

import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import se.iths.oscarp.microprojectauthserver.model.AppUser;
import se.iths.oscarp.microprojectauthserver.repository.AppUserRepository;

import java.time.LocalDateTime;

@Component
public class AppUserInit {

    // TO BE DELETED BEFORE FINAL VERSION

    private PasswordEncoder passwordEncoder;
    private AppUserRepository appUserRepository;

    public AppUserInit(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.appUserRepository = appUserRepository;
    }

    @PostConstruct
    public void createAppUser() {

        if (appUserRepository.count() == 0) {

            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRole("ADMIN");
            admin.setDateOfBirth(LocalDateTime.now().toLocalDate());
            admin.setCreatedBy("system");

            appUserRepository.save(admin);

            System.out.println("Created System Admin");

            AppUser user = new AppUser();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("12345"));
            user.setRole("USER");
            user.setDateOfBirth(LocalDateTime.now().toLocalDate());
            user.setCreatedBy("system");

            appUserRepository.save(user);

            System.out.println("Created System User");
        } else {
            System.out.println("AppUsers already exist, skipping initialization.");
        }

    }
}
