package com.lessondashboard.repository;

import com.lessondashboard.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * LessonRepository - Spring Data JPA repository for Lesson entities.
 *
 * Provides CRUD operations out of the box:
 * - findAll(), findById(), save(), deleteById(), existsById()
 *
 * Custom query methods can be added here as the app grows
 * (e.g., findByCategory, findByLevel).
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
