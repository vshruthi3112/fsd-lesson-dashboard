import LessonDashboard from './components/LessonDashboard';
import LoginForm from './components/LoginForm';
import { useAuth } from './hooks/useAuth';

/**
 * App - Root component with authentication gating.
 *
 * FLOW:
 * 1. If user is NOT logged in → show LoginForm
 * 2. If user IS logged in → show LessonDashboard
 *
 * The useAuth hook manages all auth state (user, token, login/logout).
 * We pass the user info down to LessonDashboard so it can show/hide
 * buttons based on the user's role.
 */
function App() {
  const {
    user,
    isAuthenticated,
    loading,
    error,
    login,
    register,
    logout,
    clearError,
  } = useAuth();

  // Not logged in → show login form
  if (!isAuthenticated) {
    return (
      <LoginForm
        onLogin={login}
        onRegister={register}
        loading={loading}
        error={error}
        onClearError={clearError}
      />
    );
  }

  // Logged in → show the dashboard
  return (
    <div className="app">
      <header className="app-header">
        <h1>📚 Lesson Dashboard</h1>
        <div className="user-info">
          <span className="user-badge">
            <span className="role-tag">{user?.role}</span>
          </span>
          <button className="btn-logout" onClick={logout}>
            Logout
          </button>
        </div>
      </header>
      <main>
        <LessonDashboard userRole={user?.role} />
      </main>
    </div>
  );
}

export default App;
