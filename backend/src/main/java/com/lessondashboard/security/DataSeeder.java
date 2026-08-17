package com.lessondashboard.security;

import com.lessondashboard.model.Role;
import com.lessondashboard.model.User;
import com.lessondashboard.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
            // Hash "password123" with SHA-256 first (matching what the frontend sends),
            // then BCrypt-hash that for storage.
            String sha256Hash = sha256("password123");

            // Create admin user
            User admin = new User(
                    "admin",
                    passwordEncoder.encode(sha256Hash),
                    Role.ADMIN
            );
            userRepository.save(admin);

            // Create instructor user
            User instructor = new User(
                    "instructor",
                    passwordEncoder.encode(sha256Hash),
                    Role.INSTRUCTOR
            );
            userRepository.save(instructor);

            System.out.println("✅ Default users created:");
            System.out.println("   admin / password123 (ADMIN)");
            System.out.println("   instructor / password123 (INSTRUCTOR)");
        }
    }

    /**
     * Compute SHA-256 hash of a string, returned as hex.
     * Mirrors the frontend hashPassword() function so seeded passwords
     * match what the client sends during login.
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
