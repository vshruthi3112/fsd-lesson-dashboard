import { useState, useMemo, useRef } from 'react';

// Default fields to search within
const DEFAULT_SEARCH_FIELDS = ['title', 'description'];

/**
 * useSearch - Custom hook for searching through a list of items.
 *
 * Uses useMemo to avoid re-filtering on every render — only recalculates
 * when items or query change. The searchFields reference is stabilized
 * via useRef to prevent unnecessary recomputation when callers pass
 * array literals.
 *
 * @param {Array} items - The full list of items to search through
 * @param {string[]} [searchFields=['title', 'description']] - Object keys to search within
 * @returns {{ query: string, setQuery: Function, results: Array }}
 */
export function useSearch(items, searchFields = DEFAULT_SEARCH_FIELDS) {
  const [query, setQuery] = useState('');

  // Stabilize searchFields to avoid useMemo invalidation from array literals
  const fieldsRef = useRef(searchFields);
  fieldsRef.current = searchFields;

  // useMemo ensures filtering only re-runs when items or query changes
  const results = useMemo(() => {
    if (!query.trim()) return items;

    const lowerQuery = query.toLowerCase();
    return items.filter((item) =>
      fieldsRef.current.some((field) =>
        String(item[field] || '')
          .toLowerCase()
          .includes(lowerQuery)
      )
    );
  }, [items, query]);

  return { query, setQuery, results };
}
