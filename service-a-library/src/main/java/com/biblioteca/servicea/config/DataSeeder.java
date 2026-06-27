package com.biblioteca.servicea.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.biblioteca.servicea.entity.Role;
import com.biblioteca.servicea.entity.User;
import com.biblioteca.servicea.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.findByEmail("admin@biblioteca.com").isPresent();

        if (!adminExists) {
            User admin = User.builder().fullName("Administrador")
                                .email("admin@biblioteca.com")
                                .password(passwordEncoder.encode("Admin123!"))
                                .role(Role.ADMIN)
                                .build();
            userRepository.save(admin);
            log.info("--- Admin user has been created: admin@biblioteca.com / Admin123! (The password is dummy)");
        }
    }
    
}
