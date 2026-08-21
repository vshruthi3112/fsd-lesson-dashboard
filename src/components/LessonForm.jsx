import React, { useState, useEffect } from 'react';

/**
 * LessonForm - Production-quality form for creating or editing a lesson.
 *
 * Features:
 * - Comprehensive client-side validation matching backend rules
 * - Field-level error messages for better UX
 * - Input length limits matching database constraints
 * - Input sanitization (trimming whitespace)
 *
 * Validation rules (mirror backend @Valid annotations):
 * - Title: required, 2-200 characters
 * - Description: optional, max 1000 characters
 * - Category: required, 2-100 characters
 * - Instructor: required, 2-100 characters
 * - Duration: required, 1-1440 minutes
 * - Level: required, must be Beginner/Intermediate/Advanced
 * - Date: required, YYYY-MM-DD format
 *
 * @param {Object} props
 * @param {Object|null} props.lesson - Existing lesson (null for create mode)
 * @param {Function} props.onSubmit - (lessonData) => Promise<void>
 * @param {Function} props.onCancel - Close the form
 * @param {boolean} props.saving - Whether a save is in progress
 */
function LessonForm({ lesson, onSubmit, onCancel, saving }) {
  const isEditing = Boolean(lesson);

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: '',
    instructor: '',
    duration: '',
    level: 'Beginner',
    date: '',
  });

  const [validationErrors, setValidationErrors] = useState({});

  // Populate form when editing
  useEffect(() => {
    if (lesson) {
      setFormData({
        title: lesson.title || '',
        description: lesson.description || '',
        category: lesson.category || '',
        instructor: lesson.instructor || '',
        duration: lesson.duration?.toString() || '',
        level: lesson.level || 'Beginner',
        date: lesson.date || '',
      });
    }
  }, [lesson]);

  /**
   * Handle input changes for all fields.
   * Clears the field-level error when user starts typing.
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear this field's error when user modifies it
    if (validationErrors[name]) {
      setValidationErrors((prev) => {
        const next = { ...prev };
        delete next[name];
        return next;
      });
    }
  };

  /**
   * Validate all fields and return errors object.
   * Returns empty object if all fields are valid.
   *
   * These rules match the backend @Valid annotations exactly,
   * providing instant feedback without a server round-trip.
   */
  const validateForm = () => {
    const errors = {};
    const title = formData.title.trim();
    const category = formData.category.trim();
    const instructor = formData.instructor.trim();
    const description = formData.description.trim();
    const duration = Number(formData.duration);

    // Title: required, 2-200 chars
    if (!title) {
      errors.title = 'Title is required.';
    } else if (title.length < 2) {
      errors.title = 'Title must be at least 2 characters.';
    } else if (title.length > 200) {
      errors.title = 'Title cannot exceed 200 characters.';
    }

    // Description: optional, max 1000 chars
    if (description.length > 1000) {
      errors.description = 'Description cannot exceed 1000 characters.';
    }

    // Category: required, 2-100 chars
    if (!category) {
      errors.category = 'Category is required.';
    } else if (category.length < 2) {
      errors.category = 'Category must be at least 2 characters.';
    } else if (category.length > 100) {
      errors.category = 'Category cannot exceed 100 characters.';
    }

    // Instructor: required, 2-100 chars
    if (!instructor) {
      errors.instructor = 'Instructor is required.';
    } else if (instructor.length < 2) {
      errors.instructor = 'Instructor must be at least 2 characters.';
    } else if (instructor.length > 100) {
      errors.instructor = 'Instructor cannot exceed 100 characters.';
    }

    // Duration: required, 1-1440
    if (!formData.duration) {
      errors.duration = 'Duration is required.';
    } else if (isNaN(duration) || !Number.isInteger(duration)) {
      errors.duration = 'Duration must be a whole number.';
    } else if (duration < 1) {
      errors.duration = 'Duration must be at least 1 minute.';
    } else if (duration > 1440) {
      errors.duration = 'Duration cannot exceed 1440 minutes (24 hours).';
    }

    // Date: required, YYYY-MM-DD format
    if (!formData.date) {
      errors.date = 'Date is required.';
    } else if (!/^\d{4}-\d{2}-\d{2}$/.test(formData.date)) {
      errors.date = 'Date must be in YYYY-MM-DD format.';
    }

    return errors;
  };

  /**
   * Validate and submit the form.
   * Trims string values before sending to backend.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Run all validations
    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      return;
    }

    // Build sanitized payload (trimmed strings)
    const payload = {
      title: formData.title.trim(),
      description: formData.description.trim(),
      category: formData.category.trim(),
      instructor: formData.instructor.trim(),
      duration: Number(formData.duration),
      level: formData.level,
      date: formData.date,
    };

    try {
      await onSubmit(payload);
    } catch {
      // Error is handled by the parent (useLessons hook)
    }
  };

  /**
   * Helper to show character count for fields with limits.
   */
  const charCount = (value, max) => {
    const len = value.length;
    if (len > max * 0.8) {
      return `${len}/${max}`;
    }
    return null;
  };

  return (
    <div className="lesson-form-overlay" aria-labelledby="form-title">
      <form className="lesson-form" onSubmit={handleSubmit} noValidate>
        <h2 id="form-title">{isEditing ? 'Edit Lesson' : 'Add New Lesson'}</h2>

        {/* Summary error if multiple fields have issues */}
        {Object.keys(validationErrors).length > 1 && (
          <div className="form-error" role="alert">
            Please fix the {Object.keys(validationErrors).length} errors below.
          </div>
        )}

        <div className="form-group">
          <label htmlFor="form-title-input">Title *</label>
          <input
            id="form-title-input"
            type="text"
            name="title"
            value={formData.title}
            onChange={handleChange}
            placeholder="e.g., Introduction to React Hooks"
            disabled={saving}
            maxLength={200}
            required
            aria-invalid={!!validationErrors.title}
            aria-describedby={validationErrors.title ? 'title-error' : undefined}
          />
          {validationErrors.title && (
            <span id="title-error" className="field-error" role="alert">{validationErrors.title}</span>
          )}
          {charCount(formData.title, 200) && (
            <span className="char-count">{charCount(formData.title, 200)}</span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="form-description">Description</label>
          <textarea
            id="form-description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            placeholder="Brief description of the lesson..."
            rows={3}
            disabled={saving}
            maxLength={1000}
            aria-invalid={!!validationErrors.description}
            aria-describedby={validationErrors.description ? 'desc-error' : undefined}
          />
          {validationErrors.description && (
            <span id="desc-error" className="field-error" role="alert">{validationErrors.description}</span>
          )}
          {charCount(formData.description, 1000) && (
            <span className="char-count">{charCount(formData.description, 1000)}</span>
          )}
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="form-category">Category *</label>
            <input
              id="form-category"
              type="text"
              name="category"
              value={formData.category}
              onChange={handleChange}
              placeholder="e.g., Frontend"
              disabled={saving}
              maxLength={100}
              required
              aria-invalid={!!validationErrors.category}
              aria-describedby={validationErrors.category ? 'category-error' : undefined}
            />
            {validationErrors.category && (
              <span id="category-error" className="field-error" role="alert">{validationErrors.category}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="form-instructor">Instructor *</label>
            <input
              id="form-instructor"
              type="text"
              name="instructor"
              value={formData.instructor}
              onChange={handleChange}
              placeholder="e.g., Jane Smith"
              disabled={saving}
              maxLength={100}
              required
              aria-invalid={!!validationErrors.instructor}
              aria-describedby={validationErrors.instructor ? 'instructor-error' : undefined}
            />
            {validationErrors.instructor && (
              <span id="instructor-error" className="field-error" role="alert">{validationErrors.instructor}</span>
            )}
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="form-duration">Duration (min) *</label>
            <input
              id="form-duration"
              type="number"
              name="duration"
              value={formData.duration}
              onChange={handleChange}
              placeholder="45"
              min="1"
              max="1440"
              disabled={saving}
              required
              aria-invalid={!!validationErrors.duration}
              aria-describedby={validationErrors.duration ? 'duration-error' : undefined}
            />
            {validationErrors.duration && (
              <span id="duration-error" className="field-error" role="alert">{validationErrors.duration}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="form-level">Level *</label>
            <select
              id="form-level"
              name="level"
              value={formData.level}
              onChange={handleChange}
              disabled={saving}
            >
              <option value="Beginner">Beginner</option>
              <option value="Intermediate">Intermediate</option>
              <option value="Advanced">Advanced</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="form-date">Date *</label>
            <input
              id="form-date"
              type="date"
              name="date"
              value={formData.date}
              onChange={handleChange}
              disabled={saving}
              required
              aria-invalid={!!validationErrors.date}
              aria-describedby={validationErrors.date ? 'date-error' : undefined}
            />
            {validationErrors.date && (
              <span id="date-error" className="field-error" role="alert">{validationErrors.date}</span>
            )}
          </div>
        </div>

        <div className="form-actions">
          <button
            type="button"
            className="btn-cancel"
            onClick={onCancel}
            disabled={saving}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn-save"
            disabled={saving}
          >
            {saving && 'Saving...'}
            {!saving && isEditing && 'Update Lesson'}
            {!saving && !isEditing && 'Create Lesson'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default LessonForm;
