import React from 'react';
import LessonCard from './LessonCard';

/**
 * LessonList - Renders a grid of lesson cards with action handlers.
 *
 * Shows an empty state message when no lessons match the current
 * search/filter criteria.
 *
 * Props:
 * - lessons: Array of lesson objects to display
 * - onEdit: (lesson) => void — passed to each card
 * - onDelete: (id) => void — passed to each card
 * - saving: Whether a mutation is in progress
 */
function LessonList({ lessons, onEdit, onDelete, saving }) {
  if (lessons.length === 0) {
    return (
      <div className="empty-state">
        <p>No lessons found matching your criteria.</p>
        <p>Try adjusting your search or filters.</p>
      </div>
    );
  }

  return (
    <div className="lesson-list" role="list" aria-label="Lessons">
      {lessons.map((lesson) => (
        <LessonCard
          key={lesson.id}
          lesson={lesson}
          onEdit={onEdit}
          onDelete={onDelete}
          saving={saving}
        />
      ))}
    </div>
  );
}

export default LessonList;
