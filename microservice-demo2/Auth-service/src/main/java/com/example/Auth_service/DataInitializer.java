package com.example.Auth_service;

import com.example.Auth_service.entity.AuthUser;
import com.example.Auth_service.repository.AuthUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (authUserRepository.findByUsername("admin").isPresent()) {
            AuthUser admin = authUserRepository.findByUsername("admin").get();
            admin.setPasswordHash(passwordEncoder.encode("password"));
            admin.setEnabled(true);
            authUserRepository.save(admin);
            System.out.println(">>> Reset admin password to: password");
        } else {
            AuthUser admin = new AuthUser("admin", passwordEncoder.encode("password"), "admin@example.com", null, "ADMIN");
            authUserRepository.save(admin);
            System.out.println(">>> Created admin user with password: password");
        }
    }
}
