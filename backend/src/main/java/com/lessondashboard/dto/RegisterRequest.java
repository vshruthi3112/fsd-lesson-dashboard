package com.lessondashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * RegisterRequest - DTO for registration endpoint validation.
 *
 * Enforces:
 * - Username: 3-50 chars, alphanumeric + underscores only
 * - Password: min 6 chars (received as SHA-256 hash from frontend)
 * - Role: must be ADMIN or INSTRUCTOR
 */
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 255, message = "Password must be at least 6 characters")
    private String password;

    @Pattern(regexp = "^(ADMIN|INSTRUCTOR)$", message = "Role must be ADMIN or INSTRUCTOR")
    private String role;

    // --- Constructors ---

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // --- Getters and Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
