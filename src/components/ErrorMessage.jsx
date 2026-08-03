import React from 'react';

/**
 * ErrorMessage - Displays an error with an optional retry button.
 *
 * Props:
 * - message: The error text to display
 * - onRetry: Optional callback to retry the failed operation
 *
 * In Week 6, this handles API failures gracefully.
 */
function ErrorMessage({ message, onRetry }) {
  return (
    <div className="error-message" role="alert">
      <p>⚠️ {message}</p>
      {onRetry && (
        <button className="retry-btn" onClick={onRetry}>
          Try Again
        </button>
      )}
    </div>
  );
}

export default ErrorMessage;
