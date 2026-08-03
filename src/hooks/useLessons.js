import { useReducer, useEffect, useCallback } from 'react';
import {
  fetchLessons,
  createLesson,
  updateLesson,
  deleteLesson,
} from '../services/lessonService';

// --- Action Types ---
const ACTIONS = {
  FETCH_START: 'FETCH_START',
  FETCH_SUCCESS: 'FETCH_SUCCESS',
  FETCH_ERROR: 'FETCH_ERROR',
  MUTATE_START: 'MUTATE_START',
  MUTATE_SUCCESS: 'MUTATE_SUCCESS',
  MUTATE_ERROR: 'MUTATE_ERROR',
  CLEAR_ERROR: 'CLEAR_ERROR',
};

// --- Initial State ---
const initialState = {
  lessons: [],
  loading: true,
  error: null,
  saving: false, // True during create/update/delete
};

/**
 * Reducer - manages all lesson state transitions.
 *
 * Handles both read (fetch) and write (create/update/delete) flows.
 * `saving` indicates a mutation is in progress (useful for disabling buttons).
 */
function lessonsReducer(state, action) {
  switch (action.type) {
    case ACTIONS.FETCH_START:
      return { ...state, loading: true, error: null };

    case ACTIONS.FETCH_SUCCESS:
      return { ...state, lessons: action.payload, loading: false, error: null };

    case ACTIONS.FETCH_ERROR:
      return { ...state, loading: false, error: action.payload };

    case ACTIONS.MUTATE_START:
      return { ...state, saving: true, error: null };

    case ACTIONS.MUTATE_SUCCESS:
      return { ...state, saving: false, error: null };

    case ACTIONS.MUTATE_ERROR:
      return { ...state, saving: false, error: action.payload };

    case ACTIONS.CLEAR_ERROR:
      return { ...state, error: null };

    default:
      throw new Error(`Unknown action type: ${action.type}`);
  }
}

/**
 * useLessons - Custom hook for lesson CRUD operations.
 *
 * Provides:
 * - Automatic data fetching on mount
 * - Create, update, delete mutations that refetch after success
 * - Loading/saving/error states for UI feedback
 * - Manual refetch capability
 *
 * @returns {{
 *   lessons: Array,
 *   loading: boolean,
 *   saving: boolean,
 *   error: string|null,
 *   refetch: Function,
 *   addLesson: Function,
 *   editLesson: Function,
 *   removeLesson: Function,
 *   clearError: Function
 * }}
 */
export function useLessons() {
  const [state, dispatch] = useReducer(lessonsReducer, initialState);

  /**
   * Load all lessons from the API.
   */
  const loadLessons = useCallback(async () => {
    dispatch({ type: ACTIONS.FETCH_START });
    try {
      const data = await fetchLessons();
      dispatch({ type: ACTIONS.FETCH_SUCCESS, payload: data });
    } catch (err) {
      dispatch({
        type: ACTIONS.FETCH_ERROR,
        payload: err.message || 'Failed to load lessons',
      });
    }
  }, []);

  // Fetch lessons on mount
  useEffect(() => {
    let isCancelled = false;

    async function initialLoad() {
      dispatch({ type: ACTIONS.FETCH_START });
      try {
        const data = await fetchLessons();
        if (!isCancelled) {
          dispatch({ type: ACTIONS.FETCH_SUCCESS, payload: data });
        }
      } catch (err) {
        if (!isCancelled) {
          dispatch({
            type: ACTIONS.FETCH_ERROR,
            payload: err.message || 'Failed to load lessons',
          });
        }
      }
    }

    initialLoad();
    return () => { isCancelled = true; };
  }, []);

  /**
   * Create a new lesson. Refetches the list on success.
   * @param {Object} lessonData - New lesson fields
   * @returns {Promise<Object>} The created lesson
   */
  const addLesson = useCallback(async (lessonData) => {
    dispatch({ type: ACTIONS.MUTATE_START });
    try {
      const created = await createLesson(lessonData);
      dispatch({ type: ACTIONS.MUTATE_SUCCESS });
      await loadLessons(); // Refresh list from server
      return created;
    } catch (err) {
      dispatch({
        type: ACTIONS.MUTATE_ERROR,
        payload: err.message || 'Failed to create lesson',
      });
      throw err; // Re-throw so the form can handle it
    }
  }, [loadLessons]);

  /**
   * Update an existing lesson. Refetches the list on success.
   * @param {number|string} id - Lesson ID
   * @param {Object} lessonData - Updated fields
   * @returns {Promise<Object>} The updated lesson
   */
  const editLesson = useCallback(async (id, lessonData) => {
    dispatch({ type: ACTIONS.MUTATE_START });
    try {
      const updated = await updateLesson(id, lessonData);
      dispatch({ type: ACTIONS.MUTATE_SUCCESS });
      await loadLessons();
      return updated;
    } catch (err) {
      dispatch({
        type: ACTIONS.MUTATE_ERROR,
        payload: err.message || 'Failed to update lesson',
      });
      throw err;
    }
  }, [loadLessons]);

  /**
   * Delete a lesson. Refetches the list on success.
   * @param {number|string} id - Lesson ID
   */
  const removeLesson = useCallback(async (id) => {
    dispatch({ type: ACTIONS.MUTATE_START });
    try {
      await deleteLesson(id);
      dispatch({ type: ACTIONS.MUTATE_SUCCESS });
      await loadLessons();
    } catch (err) {
      dispatch({
        type: ACTIONS.MUTATE_ERROR,
        payload: err.message || 'Failed to delete lesson',
      });
      throw err;
    }
  }, [loadLessons]);

  /**
   * Clear the current error state.
   */
  const clearError = useCallback(() => {
    dispatch({ type: ACTIONS.CLEAR_ERROR });
  }, []);

  return {
    lessons: state.lessons,
    loading: state.loading,
    saving: state.saving,
    error: state.error,
    refetch: loadLessons,
    addLesson,
    editLesson,
    removeLesson,
    clearError,
  };
}
