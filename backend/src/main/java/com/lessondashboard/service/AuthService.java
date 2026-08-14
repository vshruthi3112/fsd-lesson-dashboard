package com.lessondashboard.service;

import com.lessondashboard.model.Role;
import com.lessondashboard.model.User;
import com.lessondashboard.repository.UserRepository;
import com.lessondashboard.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * AuthService - Business logic for authentication operations.
 *
 * Handles:
 * - LOGIN: Verify credentials → generate JWT token
 * - REGISTER: Hash password → save new user → generate JWT token
 *
 * This service coordinates between:
 * - UserRepository (database access)
 * - PasswordEncoder (BCrypt hashing)
 * - JwtUtil (token generation)
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticate a user and return a JWT token.
     *
     * Steps:
     * 1. Look up the user by username
     * 2. Compare the provided password with the stored hash
     * 3. If valid, generate and return a JWT token
     *
     * @param username the login username
     * @param password the plain-text password (will be compared to hash)
     * @return JWT token string
     * @throws ResponseStatusException 401 if credentials are invalid
     */
    public String login(String username, String password) {
        // Find the user in the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        // Compare plain-text password with stored BCrypt hash
        // passwordEncoder.matches() handles the hashing internally
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        // Credentials valid! Generate a token.
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    /**
     * Register a new user and return a JWT token.
     *
     * Steps:
     * 1. Check if username is already taken
     * 2. Hash the password with BCrypt
     * 3. Save the new user
     * 4. Generate and return a JWT token
     *
     * @param username desired username
     * @param password plain-text password (will be hashed before saving)
     * @param role the role to assign (ADMIN or INSTRUCTOR)
     * @return JWT token string
     * @throws ResponseStatusException 409 if username already exists
     */
    public String register(String username, String password, Role role) {
        // Check if username is taken
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists");
        }

        // Hash the password — NEVER store plain text!
        String hashedPassword = passwordEncoder.encode(password);

        // Create and save the user
        User user = new User(username, hashedPassword, role);
        userRepository.save(user);

        // Generate a token so they're immediately logged in
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    /**
     * Look up a user's role by username.
     * Used internally to include role info in login responses.
     *
     * @param username the username to look up
     * @return the user's Role
     */
    public Role getUserRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        return user.getRole();
    }
}
