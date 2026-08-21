package com.lessondashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Lesson entity - maps to the "lesson" table in H2.
 *
 * Fields match the JSON shape expected by the React frontend:
 * { id, title, description, category, instructor, duration, level, date }
 *
 * Validation constraints enforce data integrity at the API boundary.
 * These are checked whenever @Valid is used on the controller parameter.
 */
@Entity
@Table(name = "lesson")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 100, message = "Category must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String category;

    @NotBlank(message = "Instructor is required")
    @Size(min = 2, max = 100, message = "Instructor must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String instructor;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 1440 minutes (24 hours)")
    @Column(nullable = false)
    private int duration;

    @NotBlank(message = "Level is required")
    @Pattern(regexp = "^(Beginner|Intermediate|Advanced)$",
             message = "Level must be Beginner, Intermediate, or Advanced")
    @Column(nullable = false, length = 20)
    private String level;

    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    @Column(nullable = false, length = 10)
    private String date;

    // --- Constructors ---

    public Lesson() {
    }

    public Lesson(String title, String description, String category,
                  String instructor, int duration, String level, String date) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.instructor = instructor;
        this.duration = duration;
        this.level = level;
        this.date = date;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
