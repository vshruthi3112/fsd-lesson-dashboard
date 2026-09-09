/**
 * API Client - Centralized HTTP utility for communicating with the Spring Boot backend.
 *
 * Handles:
 * - Base URL configuration
 * - JSON request/response serialization
 * - Error normalization (network errors, HTTP errors, API error messages)
 * - Consistent error format for the UI layer
 * - JWT token attachment (Authorization header)
 *
 * All service-layer functions use this client instead of calling fetch() directly.
 */

import { getToken, clearAuth } from './tokenStorage';

/**
 * Base URL for API requests.
 *
 * ENVIRONMENT VARIABLES in Vite:
 * Vite exposes env vars prefixed with VITE_ to the client code.
 * import.meta.env.VITE_API_URL is set at BUILD TIME (not runtime).
 *
 * - Development: defaults to '/api' (Vite's proxy forwards to localhost:8080)
 * - Production Docker: defaults to '/api' (Nginx reverse proxy forwards to backend)
 * - Custom: set VITE_API_URL at build time to point to a different backend
 */
const BASE_URL = import.meta.env.VITE_API_URL || '/api';

/**
 * Custom error class for API failures.
 * Carries both the HTTP status and a user-friendly message.
 */
export class ApiError extends Error {
  constructor(message, status, details = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

/**
 * Parse error response body from Spring Boot.
 * Spring Boot typically returns { message, status, timestamp } on errors.
 *
 * @param {Response} response - The fetch Response object
 * @returns {Promise<string>} Extracted error message
 */
async function parseErrorResponse(response) {
  try {
    const body = await response.json();
    return body.message || body.error || `Request failed with status ${response.status}`;
  } catch {
    return `Request failed with status ${response.status}`;
  }
}

/**
 * Core request function. All HTTP methods route through here.
 *
 * @param {string} endpoint - Path relative to /api (e.g., '/lessons')
 * @param {Object} options - Fetch options (method, body, headers, etc.)
 * @returns {Promise<any>} Parsed JSON response
 * @throws {ApiError} On HTTP errors or network failures
 */
async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  // Build headers — include JWT token if available
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // Attach JWT token to every request (if logged in)
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  // Serialize body to JSON if it's an object
  if (config.body && typeof config.body === 'object') {
    config.body = JSON.stringify(config.body);
  }

  let response;

  try {
    response = await fetch(url, config);
  } catch (err) {
    // Network error (server down, no internet, CORS, etc.)
    throw new ApiError(
      'Unable to connect to the server. Please check that the backend is running.',
      0
    );
  }

  // Handle HTTP error responses
  if (!response.ok) {
    const message = await parseErrorResponse(response);

    // If we get 401 Unauthorized, the token is invalid/expired.
    // Clear stored auth data so the user gets redirected to login.
    if (response.status === 401) {
      clearAuth();
    }

    throw new ApiError(message, response.status);
  }

  // 204 No Content (e.g., successful DELETE)
  if (response.status === 204) {
    return null;
  }

  // Parse successful JSON response
  return response.json();
}

/**
 * GET request.
 * @param {string} endpoint - API path (e.g., '/lessons')
 * @returns {Promise<any>}
 */
export function get(endpoint) {
  return request(endpoint, { method: 'GET' });
}

/**
 * POST request.
 * @param {string} endpoint - API path (e.g., '/lessons')
 * @param {Object} data - Request body
 * @returns {Promise<any>}
 */
export function post(endpoint, data) {
  return request(endpoint, { method: 'POST', body: data });
}

/**
 * PUT request.
 * @param {string} endpoint - API path (e.g., '/lessons/1')
 * @param {Object} data - Request body
 * @returns {Promise<any>}
 */
export function put(endpoint, data) {
  return request(endpoint, { method: 'PUT', body: data });
}

/**
 * DELETE request.
 * @param {string} endpoint - API path (e.g., '/lessons/1')
 * @returns {Promise<any>}
 */
export function del(endpoint) {
  return request(endpoint, { method: 'DELETE' });
}
