package com.lessondashboard.controller;

import com.lessondashboard.model.Role;
import com.lessondashboard.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login - Authenticate a user.
     *
     * Accepts username and password, returns a JWT token + user info.
     * The frontend stores the token and sends it with future requests.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Username and password are required"
            ));
        }

        // AuthService validates credentials and generates token
        String token = authService.login(username, password);
        Role role = authService.getUserRole(username);

        // Return token + user info so the frontend knows who's logged in
        Map<String, String> response = new java.util.HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("role", role.name());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register - Create a new user account.
     *
     * Accepts username, password, and role.
     * Returns a JWT token so the user is immediately logged in.
     *
     * In a real app, you might restrict who can create ADMIN accounts.
     * For this learning project, anyone can register with any role.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String roleStr = request.get("role");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Username and password are required"
            ));
        }

        // Parse the role string to enum (defaults to INSTRUCTOR if invalid)
        Role role;
        try {
            role = Role.valueOf(roleStr != null ? roleStr.toUpperCase() : "INSTRUCTOR");
        } catch (Exception e) {
            role = Role.INSTRUCTOR;
        }

        // AuthService hashes password, saves user, generates token
        String token = authService.register(username, password, role);

        Map<String, String> response = new java.util.HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("role", role.name());

        return ResponseEntity.ok(response);
    }
}
