package com.lessondashboard.model;

/**
 * Role enum - defines the authorization levels in the system.
 *
 * ADMIN       → Full access (create, read, update, delete lessons)
 * INSTRUCTOR  → Limited access (read + create lessons only)
 *
 * Stored as a string in the database (e.g., "ADMIN", "INSTRUCTOR").
 * Spring Security uses these to check permissions on each endpoint.
 */
public enum Role {
    ADMIN,
    INSTRUCTOR
}
