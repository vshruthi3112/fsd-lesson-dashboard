import { useState, useMemo, useCallback } from 'react';

/**
 * useFilter - Custom hook for multi-criteria filtering.
 *
 * Demonstrates:
 * - useState for managing filter state as an object
 * - useMemo for derived/computed state
 * - useCallback for stable function references (avoids unnecessary re-renders)
 *
 * @param {Array} items - The list of items to filter
 * @returns {{ filters: Object, setFilter: Function, clearFilters: Function, filteredItems: Array, activeFilterCount: number }}
 */
export function useFilter(items) {

  // Filters stored as key-value pairs: { category: 'Frontend', level: 'Beginner' }
  const [filters, setFilters] = useState({});

  /**
   * Set a single filter. Pass empty string to clear that filter.
   */
  const setFilter = useCallback((key, value) => {
    setFilters((prev) => {
      if (!value) {
        // Remove the filter key entirely
        const updated = { ...prev };
        delete updated[key];
        return updated;
      }
      return { ...prev, [key]: value };
    });
  }, []);

  /**
   * Clear all active filters.
   */
  const clearFilters = useCallback(() => {
    setFilters({});
  }, []);

  /**
   * Apply all active filters to the items list.
   * Each filter key must match the corresponding item property value.
   */
  const filteredItems = useMemo(() => {
    const activeFilters = Object.entries(filters);

    if (activeFilters.length === 0) return items;

    return items.filter((item) =>
      activeFilters.every(([key, value]) => item[key] === value)
    );
  }, [items, filters]);

  // Count of active filters (useful for UI badges)
  const activeFilterCount = Object.keys(filters).length;

  return { filters, setFilter, clearFilters, filteredItems, activeFilterCount };
}
