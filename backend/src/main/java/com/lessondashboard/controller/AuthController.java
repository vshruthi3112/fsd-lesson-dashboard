package com.lessondashboard.controller;

import com.lessondashboard.dto.LoginRequest;
import com.lessondashboard.dto.RegisterRequest;
import com.lessondashboard.model.Role;
import com.lessondashboard.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthController - REST endpoints for authentication.
 *
 * Endpoints:
 *   POST /api/auth/login     → Log in with username + password → get JWT token
 *   POST /api/auth/register  → Create new account → get JWT token
 *
 * These endpoints are PUBLIC (no token required) — configured in SecurityConfig.
 *
 * Request/Response shapes:
 *
 * LOGIN:
 *   Request:  { "username": "admin", "password": "password123" }
 *   Response: { "token": "eyJhbG...", "username": "admin", "role": "ADMIN" }
 *
 * REGISTER:
 *   Request:  { "username": "newuser", "password": "secret", "role": "INSTRUCTOR" }
 *   Response: { "token": "eyJhbG...", "username": "newuser", "role": "INSTRUCTOR" }
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Extract client IP address, respecting X-Forwarded-For header for proxied requests.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * POST /api/auth/login - Authenticate a user.
     *
     * Uses @Valid with LoginRequest DTO for input validation.
     * Accepts username and password, returns a JWT token + user info.
     * The frontend stores the token and sends it with future requests.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request,
                                                      HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String username = request.getUsername().trim();
        String password = request.getPassword();

        logger.info("Login attempt for user: {} from IP: {}", username, clientIp);

        // AuthService validates credentials and generates token
        String token = authService.login(username, password);
        Role role = authService.getUserRole(username);

        logger.info("Login successful for user: {} with role: {}", username, role.name());

        // Return token + user info so the frontend knows who's logged in
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("role", role.name());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register - Create a new user account.
     *
     * Uses @Valid with RegisterRequest DTO for input validation.
     * Accepts username, password, and role.
     * Returns a JWT token so the user is immediately logged in.
     *
     * In a real app, you might restrict who can create ADMIN accounts.
     * For this learning project, anyone can register with any role.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request,
                                                         HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String username = request.getUsername().trim();
        String password = request.getPassword();
        String roleStr = request.getRole();

        logger.info("Registration attempt for user: {} from IP: {}", username, clientIp);

        // Parse the role string to enum (defaults to INSTRUCTOR if null)
        Role role;
        try {
            role = Role.valueOf(roleStr != null ? roleStr.toUpperCase() : "INSTRUCTOR");
        } catch (IllegalArgumentException e) {
            role = Role.INSTRUCTOR;
            logger.warn("Invalid role '{}' provided during registration for user: {}. Defaulting to INSTRUCTOR.",
                    roleStr, username);
        }

        // AuthService hashes password, saves user, generates token
        String token = authService.register(username, password, role);

        logger.info("Registration successful for user: {} with role: {}", username, role.name());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("role", role.name());

        return ResponseEntity.ok(response);
    }
}
