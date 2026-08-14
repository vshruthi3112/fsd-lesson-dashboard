/**
 * Token Storage - Thin wrapper around localStorage for JWT token management.
 *
 * WHY IS THIS SEPARATE?
 * Both apiClient.js and authService.js need to access the token.
 * Having a shared dependency avoids circular imports:
 *   apiClient.js  → tokenStorage.js (reads token)
 *   authService.js → tokenStorage.js (reads + writes token)
 *
 * This is a common pattern: extract shared state into its own module.
 */

const TOKEN_KEY = 'lesson_dashboard_token';
const USER_KEY = 'lesson_dashboard_user';

/**
 * Get the stored JWT token.
 * @returns {string|null}
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

/**
 * Store a JWT token.
 * @param {string} token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

/**
 * Get the stored user info.
 * @returns {{username: string, role: string}|null}
 */
export function getUser() {
  const userData = localStorage.getItem(USER_KEY);
  if (!userData) return null;
  try {
    return JSON.parse(userData);
  } catch {
    return null;
  }
}

/**
 * Store user info.
 * @param {{username: string, role: string}} user
 */
export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

/**
 * Clear all auth data (logout).
 */
export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

/**
 * Check if a user is logged in (has a token).
 * @returns {boolean}
 */
export function isLoggedIn() {
  return getToken() !== null;
}
