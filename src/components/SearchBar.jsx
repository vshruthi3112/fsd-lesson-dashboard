import React from 'react';

/**
 * SearchBar - Controlled input for text search.
 *
 * Props:
 * - query: Current search string
 * - onQueryChange: Callback to update the search string
 *
 * This is a "presentational" component — no state of its own,
 * it just renders what the parent tells it to.
 */
function SearchBar({ query, onQueryChange }) {
  return (
    <div className="search-bar">
      <label htmlFor="lesson-search" className="sr-only">
        Search lessons
      </label>
      <input
        id="lesson-search"
        type="text"
        placeholder="Search lessons by title or description..."
        value={query}
        onChange={(e) => onQueryChange(e.target.value)}
        aria-label="Search lessons"
        className="search-input"
      />
      {query && (
        <button
          className="search-clear-btn"
          onClick={() => onQueryChange('')}
          aria-label="Clear search"
        >
          ✕
        </button>
      )}
    </div>
  );
}

export default SearchBar;
