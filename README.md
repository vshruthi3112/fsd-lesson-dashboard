# 📚 Lesson Dashboard

A full-stack lesson dashboard built as part of a Full Stack Development learning plan (Month 2).

- **Week 5**: React frontend — hooks, state management, search & filter
- **Week 6**: Spring Boot backend — REST API, JPA, H2 database, full CRUD
- **Week 7**: JWT authentication — login flow, role-based authorization
- **Week 8**: Production practices — structured logging, input validation, exception handling

---

## Features

- **Authentication** — Login/Register with JWT tokens
- **Role-Based Access** — Admin (full access) vs Instructor (view + create)
- **List Lessons** — Responsive card grid fetched from the backend
- **Search Lessons** — Real-time text search across titles and descriptions
- **Filter Lessons** — Filter by category and difficulty level (options derived from data)
- **Create Lesson** — Add new lessons via a modal form (Admin + Instructor)
- **Edit Lesson** — Update existing lessons in place (Admin only)
- **Delete Lesson** — Remove lessons with confirmation dialog (Admin only)
- **Loading & Error States** — Spinner, error banners, retry capability
- **Validation** — Client-side checks in the form + server-side `@Valid` annotations
- **Structured Logging** — SLF4J/Logback with level-based logging across all layers
- **Exception Handling** — Centralized `@ControllerAdvice` with structured error responses
- **Input Validation** — DTO validation for auth + entity validation for lessons with field-level error messages

---

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Java 17+ and Maven

### Frontend

```bash
npm install
npm run dev
```

Opens at [http://localhost:3000](http://localhost:3000). The Vite dev server proxies `/api/*` requests to the backend.

### Backend

```bash
cd backend
mvn spring-boot:run
```

Starts on [http://localhost:8080](http://localhost:8080). Uses an H2 in-memory database seeded with 10 lessons and 2 user accounts on every startup.

### Default Accounts

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| `admin` | `password123` | ADMIN | View, Create, Edit, Delete |
| `instructor` | `password123` | INSTRUCTOR | View, Create |

### H2 Console (optional)

Browse the database at [http://localhost:8080/h2-console](http://localhost:8080/h2-console):
- JDBC URL: `jdbc:h2:mem:lessonsdb`
- Username: `sa`
- Password: *(blank)*

### Build for Production

```bash
npm run build
```

---

## Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Frontend | React 18 (Vite) | Declarative component-based UI |
| Build Tool | Vite 5 | ES module dev server + Rollup production bundles |
| Backend | Spring Boot 3.2 | REST API framework |
| Security | Spring Security + JWT | Authentication and authorization |
| JWT Library | JJWT 0.12.5 | Token creation and validation |
| Password Hashing | BCrypt | One-way password hashing |
| Persistence | Spring Data JPA + Hibernate | ORM and repository pattern |
| Database | H2 (in-memory) | Zero-config dev database |
| Validation | Jakarta Bean Validation | `@NotBlank`, `@Min`, `@Pattern`, `@Size` |
| Logging | SLF4J + Logback | Structured logging (bundled with Spring Boot) |
| Styling | Plain CSS | Flexbox, Grid, responsive design |

---

## API Endpoints

### Authentication (Public — no token required)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/auth/login` | Log in | `{ username, password }` | `{ token, username, role }` |
| POST | `/api/auth/register` | Register | `{ username, password, role }` | `{ token, username, role }` |

### Lessons (Protected — JWT token required)

| Method | Endpoint | Description | Required Role | Response |
|--------|----------|-------------|---------------|----------|
| GET | `/api/lessons` | List all lessons | Any authenticated | `200` — JSON array |
| GET | `/api/lessons/{id}` | Get one lesson | Any authenticated | `200` — JSON object |
| POST | `/api/lessons` | Create a lesson | ADMIN or INSTRUCTOR | `201` — created lesson |
| PUT | `/api/lessons/{id}` | Update a lesson | ADMIN only | `200` — updated lesson |
| DELETE | `/api/lessons/{id}` | Delete a lesson | ADMIN only | `204` — no content |

### Authentication Header Format

All protected endpoints require:
```
Authorization: Bearer <jwt-token>
```

### Error Responses

All errors follow a consistent shape:
```json
{ "message": "...", "status": 401, "timestamp": "2026-08-07T..." }
```

Validation errors (400) include field-level details:
```json
{
  "status": 400,
  "message": "Validation failed for 2 field(s)",
  "timestamp": "2026-08-18T...",
  "errors": [
    "title: Title is required",
    "category: Category is required"
  ]
}
```

---

## Project Structure

```
├── src/                         # React Frontend
│   ├── components/
│   │   ├── LessonDashboard.jsx       # Main container (role-based button visibility)
│   │   ├── LoginForm.jsx             # Login/Register form
│   │   ├── LessonForm.jsx            # Create/Edit modal form
│   │   ├── SearchBar.jsx             # Text search input
│   │   ├── FilterPanel.jsx           # Category/Level dropdowns
│   │   ├── LessonList.jsx            # Grid of lesson cards
│   │   ├── LessonCard.jsx            # Single lesson + conditional Edit/Delete
│   │   ├── ConfirmModal.jsx          # Reusable confirmation dialog
│   │   ├── LoadingSpinner.jsx        # Loading state UI
│   │   └── ErrorMessage.jsx          # Error state + retry
│   ├── hooks/
│   │   ├── index.js                  # Barrel exports
│   │   ├── useAuth.js               # Auth state (login/logout/register)
│   │   ├── useLessons.js             # CRUD operations (useReducer + API)
│   │   ├── useSearch.js              # Text search (useState + useMemo)
│   │   └── useFilter.js              # Multi-criteria filtering
│   ├── services/
│   │   ├── tokenStorage.js           # localStorage for JWT token + user info
│   │   ├── apiClient.js              # Fetch wrapper (attaches JWT, handles errors)
│   │   ├── authService.js            # Login/register/logout (calls apiClient)
│   │   └── lessonService.js          # Lesson CRUD service (calls apiClient)
│   ├── styles/
│   │   └── index.css                 # All styles (includes auth styles)
│   ├── App.jsx                       # Root: auth gate (login vs dashboard)
│   └── main.jsx                      # Entry point
│
├── backend/                     # Spring Boot Backend
│   └── src/main/java/com/lessondashboard/
│       ├── LessonDashboardApplication.java   # @SpringBootApplication entry
│       ├── model/
│       │   ├── Lesson.java                   # JPA entity + validation
│       │   ├── User.java                     # User entity (username, password, role)
│       │   └── Role.java                     # Enum: ADMIN, INSTRUCTOR
│       ├── repository/
│       │   ├── LessonRepository.java         # Spring Data JPA repository
│       │   └── UserRepository.java           # User lookup (findByUsername)
│       ├── controller/
│       │   ├── LessonController.java         # Lesson REST endpoints
│       │   └── AuthController.java           # Login/Register endpoints
│       ├── dto/
│       │   ├── LoginRequest.java             # Login validation DTO
│       │   └── RegisterRequest.java          # Registration validation DTO
│       ├── service/
│       │   ├── LessonService.java            # Lesson business logic
│       │   └── AuthService.java              # Auth logic (credentials + token)
│       ├── security/
│       │   ├── SecurityConfig.java           # Endpoint rules + BCrypt + filter chain
│       │   ├── JwtUtil.java                  # Token generation + validation
│       │   ├── JwtFilter.java                # Intercepts requests, validates tokens
│       │   ├── CustomUserDetailsService.java # Loads users for Spring Security
│       │   └── DataSeeder.java               # Creates default users on startup
│       └── exception/
│           └── GlobalExceptionHandler.java   # Structured error responses
│   └── src/main/resources/
│       ├── application.properties            # Server + DB + JWT + Logging config
│       └── data.sql                          # Seed data (10 lessons)
│
├── backend/logs/
│   └── lesson-dashboard.log                  # Application log file (auto-rotated)
│
├── vite.config.js               # Vite config (proxy /api → localhost:8080)
├── package.json                 # Frontend dependencies + scripts
└── backend/pom.xml              # Maven dependencies (Spring Boot, JPA, H2, Security, JWT)
```

---

## Architecture

### Authentication Flow

```
┌─────────────────────────────────────────────────────────────┐
│                        LOGIN FLOW                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. User sends:  POST /api/auth/login                       │
│     Body: { "username": "admin", "password": "password123" }│
│                                                             │
│  2. Server checks credentials against the database          │
│     (BCrypt compares hashed passwords)                      │
│                                                             │
│  3. If valid → Server creates a JWT token and sends it back │
│     Response: { "token": "eyJhbG...", "role": "ADMIN" }    │
│                                                             │
│  4. Frontend stores token in localStorage                   │
│                                                             │
│  5. Every future request includes:                          │
│     Header: Authorization: Bearer eyJhbG...                 │
│                                                             │
│  6. JwtFilter validates the token on each request           │
│     - Token valid + correct role? → Allow                   │
│     - Token missing/invalid? → 401 Unauthorized             │
│     - Wrong role? → 403 Forbidden                           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Full-Stack Data Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                          BROWSER                                      │
│                                                                      │
│  App.jsx (auth gate)                                                 │
│   ├── useAuth() → login/logout/register, stores token                │
│   │                                                                  │
│   └── LessonDashboard (if authenticated)                             │
│        ├── useLessons() → lessonService → apiClient → fetch(/api/…) │
│        ├── useSearch()  → filters by text (client-side)              │
│        └── useFilter()  → filters by category/level (client-side)    │
│                                                                      │
│  apiClient.js attaches "Authorization: Bearer <token>" to every req  │
│                                                                      │
└───────────────────────────────────┬──────────────────────────────────┘
                                    │  HTTP (proxied by Vite in dev)
                                    ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      SPRING BOOT (port 8080)                         │
│                                                                      │
│  JwtFilter → validates token → sets SecurityContext                   │
│       ↓                                                              │
│  SecurityConfig → checks role against endpoint rules                  │
│       ↓                                                              │
│  Controller ──► Service ──► Repository ──► H2 DB (in-mem)            │
│                                                                      │
│  GlobalExceptionHandler → { message, status, timestamp }             │
└──────────────────────────────────────────────────────────────────────┘
```

### Frontend Layer Architecture

```
Components (render UI, show/hide based on role)
    │
    ▼
Custom Hooks (auth state, business logic, data state)
    │
    ▼
Service Layer (authService.js, lessonService.js)
    │
    ▼
API Client (apiClient.js — fetch, JWT token, error handling, JSON)
    │
    ▼
Backend REST API (protected by Spring Security + JWT)
```

### Component Tree

```
<App>
  ├── useAuth()
  │
  ├── [NOT AUTHENTICATED] → <LoginForm>
  │
  └── [AUTHENTICATED] →
        <header> (username, role badge, logout button)
        └── <LessonDashboard userRole={role}>
              ├── useLessons()         → CRUD + loading/saving/error
              ├── useSearch(lessons)   → text filtering
              ├── useFilter(results)   → category/level filtering
              │
              ├── <ErrorMessage>       (if error)
              ├── <SearchBar>          (text input)
              ├── <button>             (Add Lesson — if canCreate)
              ├── <FilterPanel>        (dropdowns)
              ├── <LessonList>
              │     └── <LessonCard>   (×N)
              │           ├── Edit button (if canEdit / ADMIN)
              │           ├── Delete button (if canDelete / ADMIN)
              │           └── <ConfirmModal>
              └── <LessonForm>         (modal, if showForm)
```

---

## Week 5 — React Concepts

| Concept | Where | Why |
|---------|-------|-----|
| `useState` | useSearch, useFilter, LessonForm | Simple state (string, object) |
| `useReducer` | useLessons | Complex interdependent state with explicit transitions |
| `useEffect` | useLessons, LessonForm | Side effects (data fetching, form population) |
| `useMemo` | useSearch, useFilter, FilterPanel | Cache expensive computations |
| `useCallback` | useLessons, useFilter | Stable function references |
| Controlled components | SearchBar, FilterPanel, LessonForm | React owns input state |
| Conditional rendering | Dashboard, LessonList | Show/hide based on state |
| Composition | Dashboard composes 3 hooks | Each hook is independent |
| Cleanup pattern | useLessons useEffect | Prevent state updates after unmount |

---

## Week 6 — Backend & Integration Concepts

| Concept | Where | Why |
|---------|-------|-----|
| `@SpringBootApplication` | LessonDashboardApplication | Auto-config + component scan |
| `@Entity` + `@Table` | Lesson.java | Maps class to database table |
| `@Id` + `@GeneratedValue` | Lesson.java | Auto-increment primary key |
| `@NotBlank` + `@Min` | Lesson.java | Declarative validation |
| `JpaRepository<T, ID>` | LessonRepository | Free CRUD methods from Spring Data |
| `@RestController` | LessonController | Returns JSON (no view resolution) |
| `@Valid @RequestBody` | Controller methods | Triggers validation before handler runs |
| `ResponseStatusException` | Controller | Throw to return HTTP error codes |
| `@RestControllerAdvice` | GlobalExceptionHandler | Centralized error handling |
| `@CrossOrigin` | Controller | Allow requests from React dev server |
| Vite Proxy | vite.config.js | Forward `/api` to backend in development |
| API Client | apiClient.js | Centralized fetch + error normalization |
| Service Layer | lessonService.js | Abstracts HTTP from hooks (one-file swap) |
| Optimistic Refetch | useLessons | Refetch list after every mutation for consistency |

---

## Week 7 — Authentication & Authorization Concepts

| Concept | Where | Why |
|---------|-------|-----|
| JWT (JSON Web Token) | JwtUtil.java | Stateless authentication token |
| BCrypt password hashing | SecurityConfig, AuthService | Secure one-way password storage |
| Spring Security filter chain | SecurityConfig.java | Request processing pipeline |
| OncePerRequestFilter | JwtFilter.java | Custom filter to validate JWT on each request |
| SecurityContext | JwtFilter.java | Spring's way of tracking "who is logged in" |
| `hasRole()` / `hasAnyRole()` | SecurityConfig.java | Declarative role-based access control |
| `permitAll()` / `authenticated()` | SecurityConfig.java | Public vs protected endpoints |
| Stateless sessions | SecurityConfig.java | No server-side session state (JWT is proof) |
| CommandLineRunner | DataSeeder.java | Run code on application startup (seed users) |
| `@Enumerated(EnumType.STRING)` | User.java | Store enum as readable string in DB |
| localStorage | tokenStorage.js | Persist JWT across page refreshes |
| Authorization header | apiClient.js | Standard way to send bearer tokens |
| Role-based UI | LessonDashboard, LessonCard | Hide buttons user can't use (UX only) |
| Auth gate pattern | App.jsx | Show login or dashboard based on auth state |
| Circular import avoidance | tokenStorage.js | Shared module prevents import cycles |

---

## Week 8 — Production Practices (Logging, Validation, Exception Handling)

### Logging

| Concept | Where | Why |
|---------|-------|-----|
| SLF4J facade | All Java classes | Decouple code from logging implementation |
| Logback (runtime) | spring-boot-starter-logging | Actual log engine (bundled with Spring Boot) |
| Parameterized messages `{}` | All logger calls | Avoid string concatenation when log level is disabled |
| Level-based filtering | application.properties | DEBUG for dev, INFO for production, WARN for noise reduction |
| File output + rotation | application.properties | `lesson-dashboard.log`, 10MB max, 30 days history |
| Log-per-layer strategy | Controller, Service, Security | Controller logs requests, Service logs business events, Security logs auth events |
| No sensitive data in logs | All classes | Never log passwords, tokens, or secrets (security standard) |

**Log levels used:**
- `DEBUG` — Per-request details (JWT validation, lesson counts) — suppressed in production
- `INFO` — State changes (login, create, update, delete, register)
- `WARN` — Client errors the app handles (bad credentials, not found, invalid tokens)
- `ERROR` — Unexpected failures (catch-all in GlobalExceptionHandler)

### Exception Handling

| Concept | Where | Why |
|---------|-------|-----|
| `@RestControllerAdvice` | GlobalExceptionHandler | Single place for all error-to-response mapping |
| `@ExceptionHandler` methods | GlobalExceptionHandler | Type-specific handling (validation, 404, 405, malformed JSON, etc.) |
| Structured error body | All error responses | Consistent `{ status, message, timestamp }` shape |
| Safe error messages | Catch-all handler | Never expose stack traces, SQL, or internal paths to client |
| Log level by category | GlobalExceptionHandler | 4xx → WARN, 5xx → ERROR (with full stack trace server-side) |
| `ResponseStatusException` | Service layer | Throw HTTP-aware exceptions from business logic |

**Exception types handled:**
| Exception | HTTP Status | Example |
|-----------|-------------|---------|
| `MethodArgumentNotValidException` | 400 | Invalid form fields |
| `HttpMessageNotReadableException` | 400 | Malformed JSON body |
| `MethodArgumentTypeMismatchException` | 400 | String where number expected |
| `ResponseStatusException` | varies | 401, 403, 404, 409 from services |
| `HttpRequestMethodNotSupportedException` | 405 | PATCH on a PUT-only endpoint |
| `NoResourceFoundException` | 404 | Non-existent URL path |
| `Exception` (catch-all) | 500 | Unexpected server errors |

### Input Validation

| Concept | Where | Why |
|---------|-------|-----|
| `@Valid` on controller params | LessonController, AuthController | Triggers validation before handler runs |
| DTO pattern | LoginRequest, RegisterRequest | Separate request validation from domain model |
| Entity validation | Lesson.java | Data integrity at the persistence boundary |
| `@NotBlank` | All required String fields | Rejects null, empty, and whitespace-only values |
| `@Size(min, max)` | Strings with length rules | Username 3-50, title 2-200, description ≤1000 |
| `@Min` / `@Max` | Numeric fields | Duration 1-1440 minutes |
| `@Pattern(regexp)` | Level, date, username | Constrain to allowed values/formats |
| Field-level error response | GlobalExceptionHandler | Returns array of "field: message" strings |

**Validation rules:**

| Field | Constraints |
|-------|-------------|
| Lesson title | Required, 2-200 chars |
| Lesson category | Required, 2-100 chars |
| Lesson instructor | Required, 2-100 chars |
| Lesson level | Required, must be Beginner/Intermediate/Advanced |
| Lesson date | Required, YYYY-MM-DD format |
| Lesson duration | 1-1440 minutes |
| Lesson description | Optional, max 1000 chars |
| Login username | Required, 3-50 chars |
| Login password | Required, min 6 chars |
| Register username | Required, 3-50 chars, alphanumeric + underscores only |
| Register password | Required, min 6 chars |
| Register role | Must be ADMIN or INSTRUCTOR |

---

## Design Decisions

1. **Service Layer Abstraction** — The frontend has three layers (hooks → service → apiClient). Swapping from mock data to a real API was a one-file change in `lessonService.js`.

2. **Composable Hooks** — `useLessons`, `useSearch`, `useFilter`, and `useAuth` are independent and compose as a pipeline in the dashboard.

3. **useReducer for CRUD state** — Six interdependent values (`lessons`, `loading`, `error`, `saving`) managed atomically. Adding new operations is just a new action type.

4. **Derived filter options** — Categories and levels are computed from whatever the backend returns, not hardcoded. New data auto-populates the dropdowns.

5. **Centralized API client** — All HTTP logic (JSON serialization, error parsing, status code handling, JWT attachment) lives in one file. Components never see `fetch()`.

6. **Server-side validation** — `@Valid` on the controller plus `@NotBlank`/`@Min` on the entity ensures data integrity regardless of client behavior.

7. **Global exception handler** — Every error response from the backend has the same JSON shape (`{ message, status, timestamp }`), which the `apiClient.js` knows how to parse.

8. **H2 in-memory + data.sql + DataSeeder** — Zero-config persistence for development. Lessons seeded via SQL, users seeded via DataSeeder (because passwords need BCrypt hashing at runtime).

9. **Proxy in development** — Vite proxies `/api` to `localhost:8080`, avoiding CORS issues without deploying to the same origin.

10. **Accessibility** — Semantic HTML, ARIA roles, `sr-only` labels, keyboard-navigable controls, `role="alert"` for errors.

11. **Defense in depth** — Backend enforces role-based access regardless of what the frontend shows. Frontend hides unauthorized buttons purely for UX. Security is NEVER frontend-only.

12. **Stateless JWT over sessions** — No server memory needed per user. Tokens are self-contained and verifiable. Trade-off: can't instantly revoke tokens (they expire naturally after 24h).

13. **Token in localStorage** — Simplest approach for a learning project. Trade-off: vulnerable to XSS. Production apps might use HttpOnly cookies instead.

14. **Separate tokenStorage module** — Prevents circular imports between apiClient (reads token) and authService (writes token) by extracting shared state into its own module.

15. **SLF4J + Logback (no extra dependencies)** — Spring Boot bundles Logback. Code logs through the SLF4J facade, keeping the implementation swappable. Parameterized `{}` placeholders avoid string concatenation overhead when log levels are disabled.

16. **Log-per-layer strategy** — Controllers log HTTP requests (DEBUG) and mutations (INFO). Services log business events and warnings. Security classes log auth decisions. This makes tracing request flow straightforward in the log file.

17. **DTO validation for auth endpoints** — `LoginRequest` and `RegisterRequest` DTOs separate request shape validation from the domain model. This lets auth validation rules evolve independently from the User entity.

18. **Field-level validation error responses** — `GlobalExceptionHandler` maps `MethodArgumentNotValidException` to an `errors` array with `"field: message"` entries, giving the frontend enough info to highlight specific form fields.

19. **Safe error responses** — The catch-all exception handler logs full stack traces server-side but returns only a generic message to the client. Internal details (SQL, paths, class names) never leak.

---

## Roadmap

- [x] Week 5: React dashboard with hooks, search, filter
- [x] Week 6: Spring Boot REST API + full CRUD integration
- [x] Week 7: JWT authentication + role-based access
- [x] Week 8: Production practices (logging, validation, exception handling)
