package com.lessondashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User entity - represents a user who can log in to the system.
 *
 * Each user has:
 * - username: unique identifier for login (3-50 chars, alphanumeric + underscores)
 * - password: stored as a BCrypt hash (NEVER store plain text passwords!)
 * - role: ADMIN or INSTRUCTOR (determines what they can do)
 *
 * This maps to the "app_user" table in H2.
 * (We avoid the table name "user" because it's a reserved word in SQL.)
 */
@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    /**
     * Role is stored as a string in the database.
     * @Enumerated(EnumType.STRING) tells JPA to save "ADMIN" or "INSTRUCTOR"
     * instead of 0 or 1 (which would be EnumType.ORDINAL — avoid that!).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // --- Constructors ---

    public User() {
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
