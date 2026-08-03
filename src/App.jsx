import React from 'react';
import LessonDashboard from './components/LessonDashboard';

/**
 * App - Root component.
 * Currently renders the Lesson Dashboard.
 * Will expand to include routing, auth, and layout in later weeks.
 */
function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>📚 Lesson Dashboard</h1>
      </header>
      <main>
        <LessonDashboard />
      </main>
    </div>
  );
}

export default App;
