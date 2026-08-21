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
- `apiClient.js` handles HTTP details (headers, serialization, error parsing, JWT token)
- `tokenStorage.js` manages localStorage for JWT token and user info (shared dependency)
- `authService.js` exposes auth functions (login, register, logout)
- `lessonService.js` exposes domain-specific functions (fetchLessons, createLesson, etc.)
- Never import `apiClient.js` directly in components or hooks — go through service files
- Never import `tokenStorage.js` in components — access auth state through `useAuth` hook

## Spring Boot Backend

- **Controller**: HTTP concerns only (routing, status codes, `@Valid`)
- **Service**: Business logic, credential validation, token generation
- **Repository**: Spring Data JPA interface — keep it clean, no custom queries unless needed
- **Model**: JPA entities with `jakarta.validation` annotations matching frontend validation
- **DTO**: Request validation classes for auth endpoints (separate from domain model)
- **Security**: JWT filter, security config, and utility classes in `security/` package
- Use constructor injection (not field injection)
- Return proper HTTP status codes (201 Created, 204 No Content, 401 Unauthorized, 403 Forbidden, 404 Not Found)
- Centralize error handling in `GlobalExceptionHandler`
- Never store plain-text passwords — always use BCrypt via `PasswordEncoder`
- Keep JWT secret in `application.properties` (use env vars in production)

## Logging

- Use SLF4J (`org.slf4j.Logger` + `LoggerFactory`) — never import Logback classes directly
- One `private static final Logger logger` per class
- Use parameterized messages (`{}` placeholders) — never string concatenation
- **Level guidelines:**
  - `DEBUG` — Per-request detail (suppressed in production): JWT validation, query results
  - `INFO` — State changes: login, CRUD operations, registration
  - `WARN` — Client errors the app handles gracefully: bad credentials, not found, invalid tokens
  - `ERROR` — Unexpected failures: catch-all exceptions (include stack trace via 3rd param)
- Never log passwords, tokens, secrets, or PII
- Controllers log at request boundary (DEBUG for reads, INFO for mutations)
- Services log business-level events and decisions
- Security classes log authentication/authorization outcomes

## Input Validation

- All controller parameters accepting request bodies must use `@Valid`
- Auth endpoints use DTO classes (`LoginRequest`, `RegisterRequest`) — not raw Map or entity
- Lesson CRUD validates directly on the `Lesson` entity (entity IS the request shape)
- Validation annotations: `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Pattern`
- Field-level error messages must be user-friendly (e.g., "Title is required")
- `GlobalExceptionHandler` maps validation exceptions to `{ status, message, timestamp, errors[] }`
- Frontend and backend validation rules must stay in sync

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
- Backend: `@RestControllerAdvice` in `GlobalExceptionHandler` returns structured JSON errors
- Consistent response shape: `{ status, message, timestamp }` for all error types
- Validation errors (400) include `errors[]` array with field-level messages
- Malformed JSON (400) returns safe message — no parse details leaked
- Type mismatches (400) describe expected type without exposing internals
- 401 responses automatically clear auth state (token expired/invalid)
- 404 for missing resources, 405 for wrong HTTP methods
- 500 catch-all logs full stack trace server-side, returns generic message to client
- Auth errors display in the login form via `useAuth` error state
- Never expose stack traces, SQL queries, or internal paths in API responses

## Authentication & Authorization

- Frontend hides UI elements based on role (UX only — never rely on this for security)
- Backend enforces role checks via Spring Security `SecurityConfig` (this IS the security)
- JWT tokens are stateless — no server sessions
- Token attached to every request via `Authorization: Bearer <token>` header
- Roles: ADMIN (full CRUD), INSTRUCTOR (view + create)
- `useAuth` hook is the single source of truth for auth state in the frontend
- Auth gate in `App.jsx` — unauthenticated users only see the login form
