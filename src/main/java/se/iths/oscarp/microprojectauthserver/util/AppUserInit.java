package se.iths.oscarp.microprojectauthserver.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import se.iths.oscarp.microprojectauthserver.model.AppUser;
import se.iths.oscarp.microprojectauthserver.repository.AppUserRepository;

@Component
public class AppUserInit {

    private AppUserRepository appUserRepository;

    public AppUserInit(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @PostConstruct
    public void createAppUser() {

        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setRole("ADMIN");
        admin.setCreatedBy("system");
        appUserRepository.save(admin);

        AppUser user = new AppUser();
        user.setUsername("user");
        user.setRole("USER");
        user.setCreatedBy("system");
        appUserRepository.save(user);

    }
}
