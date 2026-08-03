package com.lessondashboard.controller;

import com.lessondashboard.model.Lesson;
import com.lessondashboard.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LessonController - REST API endpoints for lesson CRUD.
 *
 * This layer handles HTTP concerns only:
 * - Request/response mapping
 * - Status codes
 * - Triggering validation (@Valid)
 *
 * Business logic lives in LessonService.
 *
 * Endpoints:
 *   GET    /api/lessons        → List all lessons
 *   GET    /api/lessons/{id}   → Get one lesson
 *   POST   /api/lessons        → Create a lesson
 *   PUT    /api/lessons/{id}   → Update a lesson
 *   DELETE /api/lessons/{id}   → Delete a lesson
 */
@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "http://localhost:3000") // Allow React dev server
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * GET /api/lessons - Retrieve all lessons.
     */
    @GetMapping
    public List<Lesson> getAllLessons() {
        return lessonService.getAllLessons();
    }

    /**
     * GET /api/lessons/{id} - Retrieve a single lesson by ID.
     */
    @GetMapping("/{id}")
    public Lesson getLessonById(@PathVariable Long id) {
        return lessonService.getLessonById(id);
    }

    /**
     * POST /api/lessons - Create a new lesson.
     * Returns 201 Created with the saved lesson (including generated ID).
     */
    @PostMapping
    public ResponseEntity<Lesson> createLesson(@Valid @RequestBody Lesson lesson) {
        Lesson saved = lessonService.createLesson(lesson);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * PUT /api/lessons/{id} - Update an existing lesson.
     * Returns 200 OK with the updated lesson.
     */
    @PutMapping("/{id}")
    public Lesson updateLesson(@PathVariable Long id, @Valid @RequestBody Lesson lesson) {
        return lessonService.updateLesson(id, lesson);
    }

    /**
     * DELETE /api/lessons/{id} - Delete a lesson.
     * Returns 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}
