import { useState, useCallback } from 'react';
import {
  login as loginApi,
  register as registerApi,
  logout as logoutApi,
  getUser,
  isLoggedIn,
} from '../services/authService';

/**
 * useAuth - Custom hook for authentication state management.
 *
 * Provides:
 * - Current auth state (user info, logged in status)
 * - Login/logout/register functions
 * - Loading and error states for the login form
 *
 * HOW IT WORKS:
 * - On mount, checks localStorage for an existing session (getUser/isLoggedIn)
 * - Login/register call the auth service, which stores the token
 * - Logout clears the token
 * - The component re-renders with updated state
 *
 * @returns {{
 *   user: {username: string, role: string}|null,
 *   isAuthenticated: boolean,
 *   loading: boolean,
 *   error: string|null,
 *   login: Function,
 *   register: Function,
 *   logout: Function,
 *   clearError: Function
 * }}
 */
export function useAuth() {
  // Initialize from localStorage (persists across page refreshes)
  const [user, setUser] = useState(getUser());
  const [isAuthenticated, setIsAuthenticated] = useState(isLoggedIn());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Log in with username and password.
   * On success, updates state with user info.
   * On failure, sets error message for the UI to display.
   */
  const login = useCallback(async (username, password) => {
    setLoading(true);
    setError(null);

    try {
      const response = await loginApi(username, password);
      setUser({ username: response.username, role: response.role });
      setIsAuthenticated(true);
    } catch (err) {
      setError(err.message || 'Login failed. Please try again.');
      throw err; // Re-throw so the form can handle it if needed
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Register a new account.
   * On success, the user is immediately logged in.
   */
  const register = useCallback(async (username, password, role) => {
    setLoading(true);
    setError(null);

    try {
      const response = await registerApi(username, password, role);
      setUser({ username: response.username, role: response.role });
      setIsAuthenticated(true);
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Log out the current user.
   * Clears token from localStorage and resets state.
   */
  const logout = useCallback(() => {
    logoutApi();
    setUser(null);
    setIsAuthenticated(false);
  }, []);

  /**
   * Clear the current error (e.g., when user starts typing again).
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    user,
    isAuthenticated,
    loading,
    error,
    login,
    register,
    logout,
    clearError,
  };
}
