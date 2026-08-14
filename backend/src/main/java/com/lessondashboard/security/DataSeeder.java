package com.lessondashboard.security;

import com.lessondashboard.model.Role;
import com.lessondashboard.model.User;
import com.lessondashboard.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataSeeder - Creates default user accounts on application startup.
 *
 * WHY NOT USE data.sql FOR USERS?
 * Because passwords need to be BCrypt-hashed, and we want to use Spring's
 * PasswordEncoder (which generates a random salt each time). Hardcoding
 * a BCrypt hash in SQL works but is fragile. This approach is cleaner.
 *
 * CommandLineRunner runs AFTER Spring Boot starts up and all beans are ready.
 * We check if users already exist to avoid duplicates on restart.
 *
 * DEFAULT ACCOUNTS:
 *   admin / password123       → ADMIN role (full access)
 *   instructor / password123  → INSTRUCTOR role (view + create only)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Only seed if no users exist (first startup)
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User(
                    "admin",
                    passwordEncoder.encode("password123"),
                    Role.ADMIN
            );
            userRepository.save(admin);

            // Create instructor user
            User instructor = new User(
                    "instructor",
                    passwordEncoder.encode("password123"),
                    Role.INSTRUCTOR
            );
            userRepository.save(instructor);

            System.out.println("✅ Default users created:");
            System.out.println("   admin / password123 (ADMIN)");
            System.out.println("   instructor / password123 (INSTRUCTOR)");
        }
    }
}
