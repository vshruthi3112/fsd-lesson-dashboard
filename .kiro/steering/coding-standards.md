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
- **Security**: JWT filter, security config, and utility classes in `security/` package
- Use constructor injection (not field injection)
- Return proper HTTP status codes (201 Created, 204 No Content, 401 Unauthorized, 403 Forbidden, 404 Not Found)
- Centralize error handling in `GlobalExceptionHandler`
- Never store plain-text passwords — always use BCrypt via `PasswordEncoder`
- Keep JWT secret in `application.properties` (use env vars in production)

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
- 401 responses automatically clear auth state (token expired/invalid)
- Auth errors display in the login form via `useAuth` error state

## Authentication & Authorization

- Frontend hides UI elements based on role (UX only — never rely on this for security)
- Backend enforces role checks via Spring Security `SecurityConfig` (this IS the security)
- JWT tokens are stateless — no server sessions
- Token attached to every request via `Authorization: Bearer <token>` header
- Roles: ADMIN (full CRUD), INSTRUCTOR (view + create)
- `useAuth` hook is the single source of truth for auth state in the frontend
- Auth gate in `App.jsx` — unauthenticated users only see the login form
