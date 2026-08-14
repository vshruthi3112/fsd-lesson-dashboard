import { useState } from 'react';

/**
 * LoginForm - Authentication form with login and register modes.
 *
 * Features:
 * - Toggle between "Login" and "Register" modes
 * - Form validation (required fields)
 * - Role selection (for registration)
 * - Loading state during API call
 * - Error display
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

  /**
   * Handle form submission.
   * Calls either onLogin or onRegister depending on the mode.
   */
  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevent page reload

    try {
      if (isRegisterMode) {
        await onRegister(username, password, role);
      } else {
        await onLogin(username, password);
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

        {/* Error message */}
        {error && (
          <div className="login-error" role="alert">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="login-form">
          {/* Username field */}
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => {
                setUsername(e.target.value);
                onClearError();
              }}
              placeholder="Enter your username"
              required
              disabled={loading}
              autoComplete="username"
            />
          </div>

          {/* Password field */}
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                onClearError();
              }}
              placeholder="Enter your password"
              required
              disabled={loading}
              autoComplete={isRegisterMode ? 'new-password' : 'current-password'}
            />
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
