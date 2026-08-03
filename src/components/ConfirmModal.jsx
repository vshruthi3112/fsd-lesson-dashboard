import { useEffect, useRef } from 'react';

/**
 * ConfirmModal - A simple confirmation dialog that replaces window.confirm.
 * Uses the native <dialog> element for proper accessibility and focus trapping.
 *
 * Props:
 * - open: Boolean controlling visibility
 * - title: Heading text for the modal
 * - message: Descriptive text explaining what will happen
 * - confirmLabel: Text for the confirm button (default: "Delete")
 * - cancelLabel: Text for the cancel button (default: "Cancel")
 * - onConfirm: () => void — called when the user confirms
 * - onCancel: () => void — called when the user cancels or presses Escape
 */
function ConfirmModal({
  open,
  title = 'Are you sure?',
  message,
  confirmLabel = 'Delete',
  cancelLabel = 'Cancel',
  onConfirm,
  onCancel,
}) {
  const dialogRef = useRef(null);
  const confirmBtnRef = useRef(null);

  // Show/close the dialog based on open prop
  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    if (open && !dialog.open) {
      dialog.showModal();
      confirmBtnRef.current?.focus();
    } else if (!open && dialog.open) {
      dialog.close();
    }
  }, [open]);

  // Handle native dialog cancel event (Escape key)
  const handleCancel = (e) => {
    e.preventDefault();
    onCancel();
  };

  // Close when clicking the backdrop (the ::backdrop pseudo-element)
  const handleClick = (e) => {
    if (e.target === dialogRef.current) {
      onCancel();
    }
  };

  return (
    <dialog
      className="confirm-modal-dialog"
      ref={dialogRef}
      onCancel={handleCancel}
      onClick={handleClick}
      aria-labelledby="confirm-modal-title"
      aria-describedby="confirm-modal-message"
    >
      <div className="confirm-modal">
        <h3 id="confirm-modal-title" className="confirm-modal-title">
          {title}
        </h3>
        <p id="confirm-modal-message" className="confirm-modal-message">
          {message}
        </p>
        <div className="confirm-modal-actions">
          <button className="btn-cancel" onClick={onCancel}>
            {cancelLabel}
          </button>
          <button
            className="btn-confirm-delete"
            onClick={onConfirm}
            ref={confirmBtnRef}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </dialog>
  );
}

export default ConfirmModal;
