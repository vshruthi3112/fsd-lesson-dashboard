package com.lessondashboard.security;

import com.lessondashboard.model.Role;
import com.lessondashboard.model.User;
import com.lessondashboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * DEFAULT ACCOUNTS (local development only):
 *   admin / password123       → ADMIN role (full access)
 *   instructor / password123  → INSTRUCTOR role (view + create only)
 *
 * IMPORTANT: In production, override these via environment variables or
 * disable this seeder entirely. These are for LOCAL DEVELOPMENT ONLY.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

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
            logger.info("No users found in database. Seeding default accounts for development...");

            // Hash "password123" with SHA-256 salted by username (matching what the frontend sends),
            // then BCrypt-hash that for storage.
            // Frontend sends: SHA256(username.toLowerCase() + ":" + password)

            // Create admin user
            String adminHash = sha256("admin" + ":" + "password123");
            User admin = new User(
                    "admin",
                    passwordEncoder.encode(adminHash),
                    Role.ADMIN
            );
            userRepository.save(admin);
            logger.info("Default user created: 'admin' with role ADMIN");

            // Create instructor user
            String instructorHash = sha256("instructor" + ":" + "password123");
            User instructor = new User(
                    "instructor",
                    passwordEncoder.encode(instructorHash),
                    Role.INSTRUCTOR
            );
            userRepository.save(instructor);
            logger.info("Default user created: 'instructor' with role INSTRUCTOR");

            logger.info("Data seeding complete. {} users in database.", userRepository.count());
        } else {
            logger.debug("Users already exist in database. Skipping seed.");
        }
    }

    /**
     * Compute SHA-256 hash of a string, returned as hex.
     * Mirrors the frontend hashPassword() function.
     * The input should be: username.toLowerCase() + ":" + password
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
            logger.error("SHA-256 algorithm not available - cannot seed users", e);
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
