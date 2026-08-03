/**
 * Lesson Service - Data access layer for lesson CRUD operations.
 *
 * Connects to Spring Boot REST API endpoints:
 * - GET    /api/lessons      → Fetch all lessons
 * - GET    /api/lessons/:id  → Fetch a single lesson
 * - POST   /api/lessons      → Create a new lesson
 * - PUT    /api/lessons/:id  → Update an existing lesson
 * - DELETE /api/lessons/:id  → Delete a lesson
 *
 * This abstraction keeps components/hooks decoupled from HTTP details.
 * The apiClient handles errors, serialization, and base URL.
 */

import { get, post, put, del } from './apiClient';

/**
 * Fetch all lessons from the backend.
 *
 * @returns {Promise<Array>} List of lesson objects
 * @throws {ApiError} On network or server errors
 */
export async function fetchLessons() {
  return get('/lessons');
}

/**
 * Fetch a single lesson by ID.
 *
 * @param {number|string} id - Lesson ID
 * @returns {Promise<Object>} Lesson object
 * @throws {ApiError} On 404 or other errors
 */
export async function fetchLessonById(id) {
  return get(`/lessons/${id}`);
}

/**
 * Create a new lesson.
 *
 * @param {Object} lessonData - Lesson fields (title, description, category, instructor, duration, level, date)
 * @returns {Promise<Object>} The created lesson (with generated ID)
 * @throws {ApiError} On validation errors (400) or server errors
 */
export async function createLesson(lessonData) {
  return post('/lessons', lessonData);
}

/**
 * Update an existing lesson.
 *
 * @param {number|string} id - Lesson ID
 * @param {Object} lessonData - Updated lesson fields
 * @returns {Promise<Object>} The updated lesson
 * @throws {ApiError} On 404, validation errors, or server errors
 */
export async function updateLesson(id, lessonData) {
  return put(`/lessons/${id}`, lessonData);
}

/**
 * Delete a lesson.
 *
 * @param {number|string} id - Lesson ID
 * @returns {Promise<null>} Resolves on success
 * @throws {ApiError} On 404 or server errors
 */
export async function deleteLesson(id) {
  return del(`/lessons/${id}`);
}
