package com.lessondashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Lesson Dashboard API.
 * Starts embedded Tomcat on port 8080 with H2 in-memory database.
 */
@SpringBootApplication
public class LessonDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(LessonDashboardApplication.class, args);
    }
}
