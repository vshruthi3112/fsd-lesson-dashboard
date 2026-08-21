package com.lessondashboard.service;

import com.lessondashboard.model.Lesson;
import com.lessondashboard.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * LessonService - Business logic layer for lesson operations.
 *
 * Sits between the controller (HTTP) and repository (data access).
 * Responsible for:
 * - Business validation beyond basic field checks
 * - Logging operations for audit trail
 * - Coordinating with the repository
 *
 * Logging strategy:
 * - DEBUG: Read operations (getAllLessons, getLessonById)
 * - INFO: Write operations (create, update, delete) — these change data
 * - WARN: Failed lookups (resource not found)
 */
@Service
public class LessonService {

    private static final Logger logger = LoggerFactory.getLogger(LessonService.class);

    private final LessonRepository repository;

    public LessonService(LessonRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieve all lessons.
     */
    public List<Lesson> getAllLessons() {
        List<Lesson> lessons = repository.findAll();
        logger.debug("Retrieved {} lessons from database", lessons.size());
        return lessons;
    }

    /**
     * Retrieve a single lesson by ID.
     *
     * @throws ResponseStatusException 404 if not found
     */
    public Lesson getLessonById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Lesson not found with ID: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Lesson not found with id: " + id);
                });
    }

    /**
     * Create a new lesson.
     * Ensures ID is null so the database generates it.
     */
    public Lesson createLesson(Lesson lesson) {
        lesson.setId(null); // Ensure the DB generates the ID
        Lesson saved = repository.save(lesson);
        logger.info("Lesson created - ID: {}, title: '{}', category: '{}'",
                saved.getId(), saved.getTitle(), saved.getCategory());
        return saved;
    }

    /**
     * Update an existing lesson.
     *
     * @throws ResponseStatusException 404 if not found
     */
    public Lesson updateLesson(Long id, Lesson lesson) {
        if (!repository.existsById(id)) {
            logger.warn("Update failed - lesson not found with ID: {}", id);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Lesson not found with id: " + id);
        }
        lesson.setId(id);
        Lesson updated = repository.save(lesson);
        logger.info("Lesson updated - ID: {}, title: '{}'", id, updated.getTitle());
        return updated;
    }

    /**
     * Delete a lesson by ID.
     *
     * @throws ResponseStatusException 404 if not found
     */
    public void deleteLesson(Long id) {
        if (!repository.existsById(id)) {
            logger.warn("Delete failed - lesson not found with ID: {}", id);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Lesson not found with id: " + id);
        }
        repository.deleteById(id);
        logger.info("Lesson deleted - ID: {}", id);
    }
}
