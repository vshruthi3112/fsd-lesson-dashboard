import { useState } from 'react';
import ConfirmModal from './ConfirmModal';

/**
 * LessonCard - Displays a single lesson's information with action buttons.
 *
 * Props:
 * - lesson: Object with id, title, description, category, instructor, duration, level, date
 * - onEdit: (lesson) => void — opens the edit form
 * - onDelete: (id) => void — triggers delete
 * - saving: Whether a mutation is in progress (disables action buttons)
 */
function LessonCard({ lesson, onEdit, onDelete, saving }) {
  const { id, title, description, category, instructor, duration, level, date } = lesson;
  const [confirmOpen, setConfirmOpen] = useState(false);

  const handleDeleteClick = () => {
    setConfirmOpen(true);
  };

  const handleConfirmDelete = () => {
    setConfirmOpen(false);
    onDelete(id);
  };

  const handleCancelDelete = () => {
    setConfirmOpen(false);
  };

  return (
    <article className="lesson-card" aria-label={`Lesson: ${title}`}>
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
          onClick={handleDeleteClick}
          disabled={saving}
          aria-label={`Delete ${title}`}
        >
          🗑️ Delete
        </button>
      </div>

      <ConfirmModal
        open={confirmOpen}
        title="Delete Lesson"
        message={`Delete "${title}"? This cannot be undone.`}
        confirmLabel="Delete"
        cancelLabel="Cancel"
        onConfirm={handleConfirmDelete}
        onCancel={handleCancelDelete}
      />
    </article>
  );
}

export default LessonCard;
