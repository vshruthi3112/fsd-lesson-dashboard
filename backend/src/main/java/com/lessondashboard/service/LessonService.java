package com.lessondashboard.service;

import com.lessondashboard.model.Lesson;
import com.lessondashboard.repository.LessonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * LessonService - Business logic layer for lesson operations.
 *
 * Sits between the controller (HTTP) and repository (data access).
 * Currently thin CRUD pass-through, but this is where you'd add:
 * - Authorization checks (Week 7)
 * - Complex validation rules
 * - Multi-repository orchestration
 * - Transaction management (@Transactional)
 */
@Service
public class LessonService {

    private final LessonRepository repository;

    public LessonService(LessonRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieve all lessons.
     */
    public List<Lesson> getAllLessons() {
        return repository.findAll();
    }

    /**
     * Retrieve a single lesson by ID.
     *
     * @throws ResponseStatusException 404 if not found
     */
    public Lesson getLessonById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lesson not found with id: " + id));
    }

    /**
     * Create a new lesson.
     * Ensures ID is null so the database generates it.
     */
    public Lesson createLesson(Lesson lesson) {
        lesson.setId(null);
        return repository.save(lesson);
    }

    /**
     * Update an existing lesson.
     *
     * @throws ResponseStatusException 404 if not found
     */
    public Lesson updateLesson(Long id, Lesson lesson) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Lesson not found with id: " + id);
        }
        lesson.setId(id);
        return repository.save(lesson);
    }

    /**
     * Delete a lesson by ID.
     *
     * @throws ResponseStatusException 404 if not found
     */
    public void deleteLesson(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Lesson not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
