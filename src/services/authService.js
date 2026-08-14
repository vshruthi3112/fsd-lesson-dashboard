/**
 * Auth Service - Handles login, logout, and registration.
 *
 * Connects to Spring Boot auth endpoints:
 * - POST /api/auth/login    → Authenticate user, get JWT token
 * - POST /api/auth/register → Create account, get JWT token
 *
 * Token storage is delegated to tokenStorage.js (shared with apiClient.js).
 */

import { post } from './apiClient';
import {
  setToken,
  setUser,
  clearAuth,
} from './tokenStorage';

/**
 * Log in a user.
 *
 * Sends credentials to the backend. If valid, stores the JWT token
 * and user info in localStorage.
 *
 * @param {string} username
 * @param {string} password
 * @returns {Promise<{token: string, username: string, role: string}>}
 * @throws {ApiError} On invalid credentials (401) or network errors
 */
export async function login(username, password) {
  const response = await post('/auth/login', { username, password });

  // Store token and user info
  setToken(response.token);
  setUser({ username: response.username, role: response.role });

  return response;
}

/**
 * Register a new user.
 *
 * Creates the account and stores the token (user is immediately logged in).
 *
 * @param {string} username
 * @param {string} password
 * @param {string} role - "ADMIN" or "INSTRUCTOR"
 * @returns {Promise<{token: string, username: string, role: string}>}
 * @throws {ApiError} On conflict (409 username taken) or network errors
 */
export async function register(username, password, role) {
  const response = await post('/auth/register', { username, password, role });

  setToken(response.token);
  setUser({ username: response.username, role: response.role });

  return response;
}

/**
 * Log out the current user.
 *
 * Clears all stored auth data. Since JWT is stateless,
 * there's no server-side session to invalidate.
 */
export function logout() {
  clearAuth();
}

// Re-export from tokenStorage for convenience
export { getToken, getUser, isLoggedIn } from './tokenStorage';
