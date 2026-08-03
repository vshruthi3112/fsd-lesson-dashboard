import React, { useMemo } from 'react';

/**
 * FilterPanel - Dropdowns for filtering lessons by category and level.
 *
 * Derives available filter options from the full lesson list.
 * This ensures filters always reflect available data.
 *
 * Props:
 * - lessons: Full (unfiltered) lesson array for computing options
 * - filters: Current active filter state { category: '...', level: '...' }
 * - onFilterChange: (key, value) => void
 * - onClearFilters: () => void
 * - activeFilterCount: Number of active filters
 */
function FilterPanel({ lessons, filters, onFilterChange, onClearFilters, activeFilterCount }) {
  // Derive unique categories from lessons
  const categories = useMemo(() => {
    const unique = [...new Set(lessons.map((l) => l.category))];
    return unique.sort();
  }, [lessons]);

  // Derive unique levels
  const levels = useMemo(() => {
    const unique = [...new Set(lessons.map((l) => l.level))];
    // Custom sort order for levels
    const order = ['Beginner', 'Intermediate', 'Advanced'];
    return unique.sort((a, b) => order.indexOf(a) - order.indexOf(b));
  }, [lessons]);

  return (
    <div className="filter-panel">
      <div className="filter-controls">
        {/* Category Filter */}
        <div className="filter-group">
          <label htmlFor="filter-category">Category</label>
          <select
            id="filter-category"
            value={filters.category || ''}
            onChange={(e) => onFilterChange('category', e.target.value)}
          >
            <option value="">All Categories</option>
            {categories.map((cat) => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>
        </div>

        {/* Level Filter */}
        <div className="filter-group">
          <label htmlFor="filter-level">Level</label>
          <select
            id="filter-level"
            value={filters.level || ''}
            onChange={(e) => onFilterChange('level', e.target.value)}
          >
            <option value="">All Levels</option>
            {levels.map((lvl) => (
              <option key={lvl} value={lvl}>
                {lvl}
              </option>
            ))}
          </select>
        </div>

        {/* Clear Filters */}
        {activeFilterCount > 0 && (
          <button className="clear-filters-btn" onClick={onClearFilters}>
            Clear Filters ({activeFilterCount})
          </button>
        )}
      </div>
    </div>
  );
}

export default FilterPanel;
