import { useState } from 'react';

/**
 * LoginForm - Production-quality authentication form with login and register modes.
 *
 * Features:
 * - Toggle between "Login" and "Register" modes
 * - Client-side validation matching backend DTO rules
 * - Username: 3-50 chars, alphanumeric + underscores (register mode)
 * - Password: min 6 chars (register mode)
 * - Role selection (for registration)
 * - Loading state during API call
 * - Error display with accessible alerts
 *
 * @param {Object} props
 * @param {Function} props.onLogin - Called with (username, password)
 * @param {Function} props.onRegister - Called with (username, password, role)
 * @param {boolean} props.loading - True when API call is in progress
 * @param {string|null} props.error - Error message to display
 * @param {Function} props.onClearError - Called when user starts typing
 */
function LoginForm({ onLogin, onRegister, loading, error, onClearError }) {
  // Form state
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('INSTRUCTOR');
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const [validationErrors, setValidationErrors] = useState({});

  /**
   * Validate form inputs.
   * Login mode: only checks that fields are not empty.
   * Register mode: enforces username format and password length.
   */
  const validateForm = () => {
    const errors = {};
    const trimmedUsername = username.trim();

    // Username validation
    if (!trimmedUsername) {
      errors.username = 'Username is required.';
    } else if (isRegisterMode) {
      if (trimmedUsername.length < 3) {
        errors.username = 'Username must be at least 3 characters.';
      } else if (trimmedUsername.length > 50) {
        errors.username = 'Username cannot exceed 50 characters.';
      } else if (!/^[a-zA-Z0-9_]+$/.test(trimmedUsername)) {
        errors.username = 'Username can only contain letters, numbers, and underscores.';
      }
    }

    // Password validation
    if (!password) {
      errors.password = 'Password is required.';
    } else if (isRegisterMode && password.length < 6) {
      errors.password = 'Password must be at least 6 characters.';
    }

    return errors;
  };

  /**
   * Handle form submission.
   * Validates inputs first, then calls either onLogin or onRegister.
   */
  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevent page reload

    // Client-side validation
    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      return;
    }

    setValidationErrors({});

    try {
      if (isRegisterMode) {
        await onRegister(username.trim(), password, role);
      } else {
        await onLogin(username.trim(), password);
      }
    } catch {
      // Error is handled by parent (useAuth sets error state)
    }
  };

  /**
   * Toggle between login and register modes.
   * Clears any existing errors when switching.
   */
  const toggleMode = () => {
    setIsRegisterMode(!isRegisterMode);
    setValidationErrors({});
    onClearError();
  };

  /**
   * Clear field error when user starts typing.
   */
  const handleUsernameChange = (e) => {
    setUsername(e.target.value);
    if (validationErrors.username) {
      setValidationErrors((prev) => {
        const next = { ...prev };
        delete next.username;
        return next;
      });
    }
    onClearError();
  };

  const handlePasswordChange = (e) => {
    setPassword(e.target.value);
    if (validationErrors.password) {
      setValidationErrors((prev) => {
        const next = { ...prev };
        delete next.password;
        return next;
      });
    }
    onClearError();
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1 className="login-title">
          📚 Lesson Dashboard
        </h1>
        <h2 className="login-subtitle">
          {isRegisterMode ? 'Create Account' : 'Sign In'}
        </h2>

        {/* Server error message */}
        {error && (
          <div className="login-error" role="alert">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="login-form" noValidate>
          {/* Username field */}
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={handleUsernameChange}
              placeholder="Enter your username"
              required
              disabled={loading}
              maxLength={50}
              autoComplete="username"
              aria-invalid={!!validationErrors.username}
              aria-describedby={validationErrors.username ? 'username-error' : undefined}
            />
            {validationErrors.username && (
              <span id="username-error" className="field-error" role="alert">
                {validationErrors.username}
              </span>
            )}
            {isRegisterMode && !validationErrors.username && (
              <span className="field-hint">Letters, numbers, and underscores only (3-50 chars)</span>
            )}
          </div>

          {/* Password field */}
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={handlePasswordChange}
              placeholder="Enter your password"
              required
              disabled={loading}
              autoComplete={isRegisterMode ? 'new-password' : 'current-password'}
              aria-invalid={!!validationErrors.password}
              aria-describedby={validationErrors.password ? 'password-error' : undefined}
            />
            {validationErrors.password && (
              <span id="password-error" className="field-error" role="alert">
                {validationErrors.password}
              </span>
            )}
            {isRegisterMode && !validationErrors.password && (
              <span className="field-hint">Minimum 6 characters</span>
            )}
          </div>

          {/* Role selection (only in register mode) */}
          {isRegisterMode && (
            <div className="form-group">
              <label htmlFor="role">Role</label>
              <select
                id="role"
                value={role}
                onChange={(e) => setRole(e.target.value)}
                disabled={loading}
              >
                <option value="INSTRUCTOR">Instructor (view + create)</option>
                <option value="ADMIN">Admin (full access)</option>
              </select>
            </div>
          )}

          {/* Submit button */}
          <button
            type="submit"
            className="btn-login"
            disabled={loading}
          >
            {loading
              ? 'Please wait...'
              : isRegisterMode
                ? 'Create Account'
                : 'Sign In'}
          </button>
        </form>

        {/* Toggle between login/register */}
        <p className="login-toggle">
          {isRegisterMode
            ? 'Already have an account? '
            : "Don't have an account? "}
          <button
            type="button"
            className="btn-link"
            onClick={toggleMode}
            disabled={loading}
          >
            {isRegisterMode ? 'Sign In' : 'Create Account'}
          </button>
        </p>
      </div>
    </div>
  );
}

export default LoginForm;
