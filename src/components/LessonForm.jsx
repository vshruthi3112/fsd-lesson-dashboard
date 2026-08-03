import React, { useState, useEffect } from 'react';

/**
 * LessonForm - Form for creating or editing a lesson.
 *
 * Props:
 * - lesson: Existing lesson object (null for create mode)
 * - onSubmit: (lessonData) => Promise<void> — called on form submission
 * - onCancel: () => void — close the form
 * - saving: Whether a save is in progress (disables the form)
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

  const [validationError, setValidationError] = useState('');

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
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setValidationError('');
  };

  /**
   * Validate and submit the form.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Basic client-side validation
    if (!formData.title.trim()) {
      setValidationError('Title is required.');
      return;
    }
    if (!formData.category.trim()) {
      setValidationError('Category is required.');
      return;
    }
    if (!formData.instructor.trim()) {
      setValidationError('Instructor is required.');
      return;
    }
    if (!formData.duration || Number(formData.duration) <= 0) {
      setValidationError('Duration must be a positive number.');
      return;
    }
    if (!formData.date) {
      setValidationError('Date is required.');
      return;
    }

    const payload = {
      ...formData,
      duration: Number(formData.duration),
    };

    try {
      await onSubmit(payload);
    } catch {
      // Error is handled by the parent (useLessons hook)
    }
  };

  return (
    <div className="lesson-form-overlay" aria-labelledby="form-title">
      <form className="lesson-form" onSubmit={handleSubmit} noValidate>
        <h2 id="form-title">{isEditing ? 'Edit Lesson' : 'Add New Lesson'}</h2>

        {validationError && (
          <div className="form-error" role="alert">
            {validationError}
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
            required
          />
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
          />
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
              required
            />
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
              required
            />
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
              disabled={saving}
              required
            />
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
            />
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
