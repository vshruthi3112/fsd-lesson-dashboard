package com.lessondashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * RootController - Handles the root URL (/).
 *
 * Without this, hitting the base URL returns a 403 from Spring Security
 * because there's no mapped resource and the catch-all rule requires
 * authentication. This provides a friendly JSON response instead.
 */
@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
            "service", "Lesson Dashboard API",
            "status", "running",
            "health", "/api/auth/health",
            "docs", "Use /api/auth/login (POST) to authenticate"
        ));
    }
}
