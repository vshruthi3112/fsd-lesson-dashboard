---
inclusion: always
---

# Coding Standards

## React Components

- Use functional components only (no class components)
- One component per file, named same as the file (PascalCase)
- Destructure props in the function signature
- Add JSDoc comments explaining the component's purpose and props
- Keep components focused — if it does more than one thing, split it
- Components should not call APIs directly — use hooks from `src/hooks/`

## Custom Hooks

- Prefix with `use` (e.g., `useLessons`, `useSearch`)
- Return an object (not an array) for named destructuring
- Document return values with JSDoc
- Handle loading, error, and success states
- Include cleanup in useEffect where needed (cancellation flags, abort controllers)
- Use `useReducer` for complex multi-state flows (e.g., loading + saving + error)
- Use `useCallback` for functions passed down or used in dependency arrays
- Use `useMemo` for expensive derived computations

## Service Layer

- All functions return Promises
- Each function has a JSDoc comment describing what API endpoint it maps to
- `apiClient.js` handles HTTP details (headers, serialization, error parsing)
- `lessonService.js` exposes domain-specific functions (fetchLessons, createLesson, etc.)
- Never import `apiClient.js` directly in components or hooks — go through service files

## Spring Boot Backend

- **Controller**: HTTP concerns only (routing, status codes, `@Valid`)
- **Service**: Business logic, authorization (future), orchestration
- **Repository**: Spring Data JPA interface — keep it clean, no custom queries unless needed
- **Model**: JPA entities with `jakarta.validation` annotations matching frontend validation
- Use constructor injection (not field injection)
- Return proper HTTP status codes (201 Created, 204 No Content, 404 Not Found)
- Centralize error handling in `GlobalExceptionHandler`

## File Organization

- Barrel exports via `index.js` in hook directories
- Group by feature, not by file type, as the app grows
- Keep styles in `src/styles/` (one file per major component if needed)
- Backend follows standard Spring package structure (controller/service/repository/model)

## Accessibility

- All interactive elements must have accessible labels
- Use semantic HTML (article, main, nav, section, etc.)
- Include `role` and `aria-*` attributes where appropriate
- Ensure keyboard navigability
- Form inputs need associated `<label>` elements with `htmlFor`
- Error messages should use `role="alert"`

## Error Handling

- Frontend: `ApiError` class in `apiClient.js` carries status + message
- Hooks surface errors via state; components render `ErrorMessage` with retry
- Backend: `@ControllerAdvice` returns structured JSON errors `{ message, status, timestamp }`
- Validation errors (400) include field-level messages
