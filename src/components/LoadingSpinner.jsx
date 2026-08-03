import React from 'react';

/**
 * LoadingSpinner - Displays a loading indicator.
 *
 * Props:
 * - message: Optional text to show below the spinner
 *
 * In Week 6, this will be used during API calls.
 */
function LoadingSpinner({ message = 'Loading...' }) {
  return (
    <div className="loading-spinner" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true"></div>
      <p>{message}</p>
    </div>
  );
}

export default LoadingSpinner;
