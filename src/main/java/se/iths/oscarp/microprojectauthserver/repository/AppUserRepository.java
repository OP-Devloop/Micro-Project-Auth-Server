package se.iths.oscarp.microprojectauthserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.iths.oscarp.microprojectauthserver.model.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
}