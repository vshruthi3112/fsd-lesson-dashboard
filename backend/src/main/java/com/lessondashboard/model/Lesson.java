package com.lessondashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Lesson entity - maps to the "lesson" table in H2.
 *
 * Fields match the JSON shape expected by the React frontend:
 * { id, title, description, category, instructor, duration, level, date }
 */
@Entity
@Table(name = "lesson")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    private String description;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    @NotBlank(message = "Instructor is required")
    @Column(nullable = false)
    private String instructor;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(nullable = false)
    private int duration;

    @NotBlank(message = "Level is required")
    @Column(nullable = false)
    private String level;

    @NotBlank(message = "Date is required")
    @Column(nullable = false)
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
