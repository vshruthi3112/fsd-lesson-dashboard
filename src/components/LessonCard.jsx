import React from 'react';

/**
 * LessonCard - Displays a single lesson's information with action buttons.
 *
 * Props:
 * - lesson: Object with id, title, description, category, instructor, duration, level, date
 * - onEdit: (lesson) => void — opens the edit form
 * - onDelete: (id) => void — triggers delete confirmation
 * - saving: Whether a mutation is in progress (disables action buttons)
 */
function LessonCard({ lesson, onEdit, onDelete, saving }) {
  const { id, title, description, category, instructor, duration, level, date } = lesson;

  const handleDelete = () => {
    if (window.confirm(`Delete "${title}"? This cannot be undone.`)) {
      onDelete(id);
    }
  };

  return (
    <article className="lesson-card" role="listitem">
      <div className="lesson-card-header">
        <h3 className="lesson-title">{title}</h3>
        <span className={`lesson-level level-${level.toLowerCase()}`}>{level}</span>
      </div>

      <p className="lesson-description">{description}</p>

      <div className="lesson-meta">
        <span className="lesson-category" title="Category">
          📂 {category}
        </span>
        <span className="lesson-instructor" title="Instructor">
          👤 {instructor}
        </span>
        <span className="lesson-duration" title="Duration">
          ⏱️ {duration} min
        </span>
        <span className="lesson-date" title="Published date">
          📅 {new Date(date).toLocaleDateString()}
        </span>
      </div>

      <div className="lesson-card-actions">
        <button
          className="btn-edit"
          onClick={() => onEdit(lesson)}
          disabled={saving}
          aria-label={`Edit ${title}`}
        >
          ✏️ Edit
        </button>
        <button
          className="btn-delete"
          onClick={handleDelete}
          disabled={saving}
          aria-label={`Delete ${title}`}
        >
          🗑️ Delete
        </button>
      </div>
    </article>
  );
}

export default LessonCard;
