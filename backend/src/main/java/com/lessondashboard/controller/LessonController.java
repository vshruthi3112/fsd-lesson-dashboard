package com.lessondashboard.controller;

import com.lessondashboard.model.Lesson;
import com.lessondashboard.service.LessonService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * - Logging request/response at the controller boundary
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

    private static final Logger logger = LoggerFactory.getLogger(LessonController.class);

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * GET /api/lessons - Retrieve all lessons.
     */
    @GetMapping
    public List<Lesson> getAllLessons() {
        logger.debug("GET /api/lessons - Fetching all lessons");
        List<Lesson> lessons = lessonService.getAllLessons();
        logger.debug("Returning {} lessons", lessons.size());
        return lessons;
    }

    /**
     * GET /api/lessons/{id} - Retrieve a single lesson by ID.
     */
    @GetMapping("/{id}")
    public Lesson getLessonById(@PathVariable Long id) {
        logger.debug("GET /api/lessons/{} - Fetching lesson", id);
        return lessonService.getLessonById(id);
    }

    /**
     * POST /api/lessons - Create a new lesson.
     * Returns 201 Created with the saved lesson (including generated ID).
     * @Valid triggers the validation annotations on the Lesson model.
     */
    @PostMapping
    public ResponseEntity<Lesson> createLesson(@Valid @RequestBody Lesson lesson) {
        logger.info("POST /api/lessons - Creating lesson: '{}'", lesson.getTitle());
        Lesson saved = lessonService.createLesson(lesson);
        logger.info("Lesson created successfully with ID: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * PUT /api/lessons/{id} - Update an existing lesson.
     * Returns 200 OK with the updated lesson.
     * @Valid triggers the validation annotations on the Lesson model.
     */
    @PutMapping("/{id}")
    public Lesson updateLesson(@PathVariable Long id, @Valid @RequestBody Lesson lesson) {
        logger.info("PUT /api/lessons/{} - Updating lesson", id);
        Lesson updated = lessonService.updateLesson(id, lesson);
        logger.info("Lesson {} updated successfully", id);
        return updated;
    }

    /**
     * DELETE /api/lessons/{id} - Delete a lesson.
     * Returns 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        logger.info("DELETE /api/lessons/{} - Deleting lesson", id);
        lessonService.deleteLesson(id);
        logger.info("Lesson {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}
