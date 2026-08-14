import { useState } from 'react';
import { useLessons } from '../hooks/useLessons';
import { useSearch } from '../hooks/useSearch';
import { useFilter } from '../hooks/useFilter';
import SearchBar from './SearchBar';
import FilterPanel from './FilterPanel';
import LessonList from './LessonList';
import LessonForm from './LessonForm';
import LoadingSpinner from './LoadingSpinner';
import ErrorMessage from './ErrorMessage';

/**
 * LessonDashboard - Main container component for the CRUD lesson app.
 *
 * Orchestrates:
 * - Data fetching (useLessons)
 * - Search (useSearch)
 * - Filtering (useFilter)
 * - Create/Edit form visibility
 * - Delete operations
 * - Role-based access control (show/hide buttons based on user role)
 *
 * Data Flow:
 * 1. useLessons() fetches all lessons + provides CRUD mutations
 * 2. useSearch() narrows by text query
 * 3. useFilter() further narrows by category/level
 *
 * Role Permissions:
 * - ADMIN: can create, edit, delete
 * - INSTRUCTOR: can create only (no edit/delete)
 *
 * @param {Object} props
 * @param {string} props.userRole - "ADMIN" or "INSTRUCTOR"
 */
function LessonDashboard({ userRole }) {
  // CRUD state and operations
  const {
    lessons,
    loading,
    saving,
    error,
    refetch,
    addLesson,
    editLesson,
    removeLesson,
    clearError,
  } = useLessons();

  // Search on the full lesson list
  const { query, setQuery, results: searchResults } = useSearch(lessons);

  // Filter on search results
  const { filters, setFilter, clearFilters, filteredItems, activeFilterCount } =
    useFilter(searchResults);

  // Form visibility state
  const [showForm, setShowForm] = useState(false);
  const [editingLesson, setEditingLesson] = useState(null);

  // --- Role-based permissions ---
  const canCreate = userRole === 'ADMIN' || userRole === 'INSTRUCTOR';
  const canEdit = userRole === 'ADMIN';
  const canDelete = userRole === 'ADMIN';

  // --- Form Handlers ---

  const handleAdd = () => {
    setEditingLesson(null);
    setShowForm(true);
  };

  const handleEdit = (lesson) => {
    setEditingLesson(lesson);
    setShowForm(true);
  };

  const handleFormSubmit = async (lessonData) => {
    if (editingLesson) {
      await editLesson(editingLesson.id, lessonData);
    } else {
      await addLesson(lessonData);
    }
    setShowForm(false);
    setEditingLesson(null);
  };

  const handleFormCancel = () => {
    setShowForm(false);
    setEditingLesson(null);
  };

  const handleDelete = async (id) => {
    try {
      await removeLesson(id);
    } catch {
      // Error is already in state via useLessons
    }
  };

  // --- Render ---

  if (loading) {
    return <LoadingSpinner message="Loading lessons..." />;
  }

  return (
    <div className="lesson-dashboard">
      {/* Error Banner */}
      {error && (
        <ErrorMessage
          message={error}
          onRetry={() => {
            clearError();
            refetch();
          }}
        />
      )}

      {/* Toolbar: Search + Add Button */}
      <div className="dashboard-toolbar">
        <SearchBar query={query} onQueryChange={setQuery} />
        {canCreate && (
          <button className="btn-add" onClick={handleAdd} disabled={saving}>
            ➕ Add Lesson
          </button>
        )}
      </div>

      {/* Filters */}
      <FilterPanel
        lessons={lessons}
        filters={filters}
        onFilterChange={setFilter}
        onClearFilters={clearFilters}
        activeFilterCount={activeFilterCount}
      />

      {/* Results Summary */}
      <p className="results-summary">
        Showing {filteredItems.length} of {lessons.length} lessons
        {query && <span> matching &quot;{query}&quot;</span>}
      </p>

      {/* Lesson List */}
      <LessonList
        lessons={filteredItems}
        onEdit={canEdit ? handleEdit : null}
        onDelete={canDelete ? handleDelete : null}
        saving={saving}
      />

      {/* Create/Edit Form (modal overlay) */}
      {showForm && (
        <LessonForm
          lesson={editingLesson}
          onSubmit={handleFormSubmit}
          onCancel={handleFormCancel}
          saving={saving}
        />
      )}
    </div>
  );
}

export default LessonDashboard;
