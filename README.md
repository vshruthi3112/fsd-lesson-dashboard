# 📚 Lesson Dashboard

A full-stack lesson dashboard built as part of a Full Stack Development learning plan (Month 2).

- **Week 5**: React frontend — hooks, state management, search & filter
- **Week 6**: Spring Boot backend — REST API, JPA, H2 database, full CRUD

---

## Features

- **List Lessons** — Responsive card grid fetched from the backend
- **Search Lessons** — Real-time text search across titles and descriptions
- **Filter Lessons** — Filter by category and difficulty level (options derived from data)
- **Create Lesson** — Add new lessons via a modal form
- **Edit Lesson** — Update existing lessons in place
- **Delete Lesson** — Remove lessons with confirmation dialog
- **Loading & Error States** — Spinner, error banners, retry capability
- **Validation** — Client-side checks in the form + server-side `@Valid` annotations

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

Starts on [http://localhost:8080](http://localhost:8080). Uses an H2 in-memory database seeded with 10 lessons on every startup.

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
| Persistence | Spring Data JPA + Hibernate | ORM and repository pattern |
| Database | H2 (in-memory) | Zero-config dev database |
| Validation | Jakarta Bean Validation | `@NotBlank`, `@Min` annotations |
| Styling | Plain CSS | Flexbox, Grid, responsive design |

---

## API Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/lessons` | List all lessons | `200` — JSON array |
| GET | `/api/lessons/{id}` | Get one lesson | `200` — JSON object |
| POST | `/api/lessons` | Create a lesson | `201` — created lesson |
| PUT | `/api/lessons/{id}` | Update a lesson | `200` — updated lesson |
| DELETE | `/api/lessons/{id}` | Delete a lesson | `204` — no content |

Error responses follow a consistent shape: `{ message, status, timestamp }`.

---

## Project Structure

```
├── src/                         # React Frontend
│   ├── components/
│   │   ├── LessonDashboard.jsx       # Main container (orchestrates hooks + CRUD)
│   │   ├── LessonForm.jsx            # Create/Edit modal form
│   │   ├── SearchBar.jsx             # Text search input
│   │   ├── FilterPanel.jsx           # Category/Level dropdowns
│   │   ├── LessonList.jsx            # Grid of lesson cards
│   │   ├── LessonCard.jsx            # Single lesson + Edit/Delete buttons
│   │   ├── ConfirmModal.jsx          # Reusable confirmation dialog (native <dialog>)
│   │   ├── LoadingSpinner.jsx        # Loading state UI
│   │   └── ErrorMessage.jsx          # Error state + retry
│   ├── hooks/
│   │   ├── index.js                  # Barrel exports
│   │   ├── useLessons.js             # CRUD operations (useReducer + API)
│   │   ├── useSearch.js              # Text search (useState + useMemo)
│   │   └── useFilter.js              # Multi-criteria filtering
│   ├── services/
│   │   ├── apiClient.js              # Centralized fetch wrapper (errors, JSON)
│   │   └── lessonService.js          # Lesson CRUD service (calls apiClient)
│   ├── styles/
│   │   └── index.css                 # All styles
│   ├── App.jsx                       # Root component
│   └── main.jsx                      # Entry point
│
├── backend/                     # Spring Boot Backend
│   └── src/main/java/com/lessondashboard/
│       ├── LessonDashboardApplication.java   # @SpringBootApplication entry
│       ├── model/
│       │   └── Lesson.java                   # JPA entity + validation
│       ├── repository/
│       │   └── LessonRepository.java         # Spring Data JPA repository
│       ├── controller/
│       │   └── LessonController.java         # REST endpoints
|       |__ service/
|       |   └── LessonService.java            # Backend business logic
│       └── exception/
│           └── GlobalExceptionHandler.java   # Structured error responses
│   └── src/main/resources/
│       ├── application.properties            # Server + DB config
│       └── data.sql                          # Seed data (10 lessons)
│
├── vite.config.js               # Vite config (proxy /api → localhost:8080)
├── package.json                 # Frontend dependencies + scripts
└── backend/pom.xml              # Maven dependencies (Spring Boot, JPA, H2)
```

---

## Architecture

### Full-Stack Data Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                          BROWSER                                      │
│                                                                      │
│  LessonDashboard                                                     │
│   ├── useLessons() → lessonService → apiClient → fetch(/api/...)     │
│   ├── useSearch()  → filters by text (client-side)                   │
│   └── useFilter()  → filters by category/level (client-side)         │
│                                                                      │
└───────────────────────────────┬──────────────────────────────────────┘
                                │  HTTP (proxied by Vite in dev)
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      SPRING BOOT (port 8080)                         │
│                                                                      │
│  LessonController ──► LessonService ──► LessonRepository ──► H2 DB   │
│       @Valid           Business logic      JPA/Hibernate    (in-mem) │
│                                                                      │
│  GlobalExceptionHandler → { message, status, timestamp }             │
└──────────────────────────────────────────────────────────────────────┘
```

### Frontend Layer Architecture

```
Components (render UI)
    │
    ▼
Custom Hooks (business logic, state)
    │
    ▼
Service Layer (lessonService.js)
    │
    ▼
API Client (apiClient.js — fetch, error handling, JSON)
    │
    ▼
Backend REST API
```

Components never call the service or API directly. They always go through hooks.

### Component Tree

```
<App>
  └── <LessonDashboard>   ← orchestrator
        ├── useLessons()         → CRUD + loading/saving/error
        ├── useSearch(lessons)   → text filtering
        ├── useFilter(results)   → category/level filtering
        │
        ├── <ErrorMessage>       (if error)
        ├── <SearchBar>          (text input)
        ├── <button>             (Add Lesson)
        ├── <FilterPanel>        (dropdowns)
        ├── <LessonList>
        │     └── <LessonCard>   (×N, with Edit/Delete)
        │           └── <ConfirmModal>  (delete confirmation)
        ├── <LoadingSpinner>     (if loading)
        └── <LessonForm>         (modal, if showForm)
```

### State Management (useReducer)

```
dispatch(action)
       │
       ▼
┌────────────────────────────────────────────────────┐
│              lessonsReducer                         │
│                                                    │
│  FETCH_START      → loading: true                  │
│  FETCH_SUCCESS    → lessons: [...], loading: false  │
│  FETCH_ERROR      → error: "...", loading: false   │
│  MUTATE_START     → saving: true                   │
│  MUTATE_SUCCESS   → saving: false                  │
│  MUTATE_ERROR     → error: "...", saving: false    │
│  CLEAR_ERROR      → error: null                    │
└────────────────────────────────────────────────────┘
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

## Design Decisions

1. **Service Layer Abstraction** — The frontend has three layers (hooks → service → apiClient). Swapping from mock data to a real API was a one-file change in `lessonService.js`.

2. **Composable Hooks** — `useLessons`, `useSearch`, and `useFilter` are independent and compose as a pipeline in the dashboard.

3. **useReducer for CRUD state** — Six interdependent values (`lessons`, `loading`, `error`, `saving`) managed atomically. Adding new operations is just a new action type.

4. **Derived filter options** — Categories and levels are computed from whatever the backend returns, not hardcoded. New data auto-populates the dropdowns.

5. **Centralized API client** — All HTTP logic (JSON serialization, error parsing, status code handling) lives in one file. Components never see `fetch()`.

6. **Server-side validation** — `@Valid` on the controller plus `@NotBlank`/`@Min` on the entity ensures data integrity regardless of client behavior.

7. **Global exception handler** — Every error response from the backend has the same JSON shape (`{ message, status, timestamp }`), which the `apiClient.js` knows how to parse.

8. **H2 in-memory + data.sql** — Zero-config persistence for development. The seed data matches the original Week 5 mock lessons for continuity.

9. **Proxy in development** — Vite proxies `/api` to `localhost:8080`, avoiding CORS issues without deploying to the same origin. The `@CrossOrigin` annotation is a fallback.

10. **Accessibility** — Semantic HTML, ARIA roles, `sr-only` labels, keyboard-navigable controls, `role="alert"` for errors.

11. **Native `<dialog>` for modals** — The `ConfirmModal` uses the HTML `<dialog>` element with `showModal()`, giving us free focus trapping, backdrop, and Escape-to-close behavior without extra libraries.

---

## File-by-File Explanation

### Backend

#### `LessonDashboardApplication.java`
Entry point. `@SpringBootApplication` combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`. Starts embedded Tomcat on port 8080.

#### `Lesson.java` (Entity)
JPA entity mapped to the `lesson` table. Fields match the JSON shape the frontend expects. Validation annotations (`@NotBlank`, `@Min`) are enforced when `@Valid` is used in the controller.

#### `LessonRepository.java`
Extends `JpaRepository<Lesson, Long>` — provides `findAll()`, `findById()`, `save()`, `deleteById()`, `existsById()` for free. No implementation needed.

#### `LessonController.java`
Five endpoints covering full CRUD. Uses constructor injection (no `@Autowired` on fields). Returns `ResponseEntity` for create (201) and delete (204). Throws `ResponseStatusException` for 404s.

#### `GlobalExceptionHandler.java`
`@RestControllerAdvice` catches three exception types:
- `MethodArgumentNotValidException` → 400 with field error messages
- `ResponseStatusException` → whatever status was thrown
- `Exception` → 500 with a safe generic message

#### `application.properties`
Configures H2 connection, enables the H2 console, defers data initialization so `data.sql` runs after Hibernate creates the schema.

#### `data.sql`
10 INSERT statements matching the original mock lessons. Runs on every startup (H2 is in-memory, so data resets each time).

### Frontend

#### `vite.config.js`
Proxy configuration: requests to `/api` are forwarded to `http://localhost:8080`. This lets the frontend call `fetch('/api/lessons')` without specifying a host.

#### `apiClient.js`
Centralized HTTP wrapper. Features:
- `ApiError` class with `message`, `status`, and `details`
- `parseErrorResponse()` reads the backend's `{ message }` JSON
- Network errors get a user-friendly message
- 204 responses return `null` (for DELETE)
- Auto-serializes objects to JSON

#### `lessonService.js`
Thin mapping of CRUD operations to HTTP verbs. Each function is one line calling the apiClient. This is the only file that changed when moving from mock data to real API calls.

#### `useLessons.js`
Upgraded from fetch-only to full CRUD:
- `addLesson(data)` → POST, then refetch
- `editLesson(id, data)` → PUT, then refetch
- `removeLesson(id)` → DELETE, then refetch
- `saving` state disables the UI during mutations
- Errors are caught and surfaced via reducer state

#### `LessonDashboard.jsx`
Orchestrator with form state management:
- `showForm` / `editingLesson` control the modal
- `handleAdd`, `handleEdit`, `handleFormSubmit`, `handleFormCancel`, `handleDelete`
- Passes CRUD callbacks down to child components

#### `LessonForm.jsx`
Modal form for create and edit. Features:
- Pre-populates fields when editing (`useEffect` on `lesson` prop)
- Client-side validation before submission
- Disabled state while `saving` is true
- Accessible labels for every input

#### `ConfirmModal.jsx`
A reusable confirmation dialog built on the native `<dialog>` element. Props: `open`, `title`, `message`, `confirmLabel`, `cancelLabel`, `onConfirm`, `onCancel`. Handles Escape key, backdrop click, and auto-focuses the confirm button. Used by `LessonCard` for delete confirmation.

#### `LessonCard.jsx`
Displays lesson data with Edit and Delete buttons. Delete opens a `ConfirmModal` for user confirmation before calling the handler.

#### `LessonList.jsx`
Passes `onEdit`, `onDelete`, and `saving` through to each `LessonCard`. Renders an empty state when no lessons match.

---

## Create Lesson Flow

The diagram below traces the full path when a user creates/adds a new lesson — from the button click all the way to the database and back.

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              USER INTERACTION                                   │
│                                                                                 │
│  1. User clicks "➕ Add Lesson" button in LessonDashboard                       │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  LessonDashboard.jsx                                                            │
│                                                                                 │
│  2. handleAdd() sets showForm=true, editingLesson=null                          │
│  3. Renders <LessonForm lesson={null} onSubmit={handleFormSubmit} />            │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  LessonForm.jsx (modal overlay)                                                 │
│                                                                                 │
│  4. User fills in: title, description, category, instructor, duration,          │
│     level, date                                                                 │
│  5. User clicks "Create Lesson" button                                          │
│  6. handleSubmit() runs client-side validation                                  │
│     - If invalid → shows validation error, stops here                           │
│     - If valid → builds payload object, calls onSubmit(payload)                 │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  LessonDashboard.jsx → handleFormSubmit(lessonData)                             │
│                                                                                 │
│  7. Calls addLesson(lessonData)  (from useLessons hook)                         │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  useLessons.js (custom hook)                                                    │
│                                                                                 │
│  8. dispatch({ type: MUTATE_START })  → sets saving=true in state               │
│  9. Calls createLesson(lessonData)    (from lessonService)                      │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  lessonService.js                                                               │
│                                                                                 │
│  10. createLesson(data) → calls post('/lessons', data) from apiClient           │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  apiClient.js                                                                   │
│                                                                                 │
│  11. request('/lessons', { method: 'POST', body: data })                        │
│      - Builds URL: /api/lessons                                                 │
│      - Sets headers: Content-Type: application/json                             │
│      - Serializes body: JSON.stringify(data)                                    │
│      - Calls fetch(url, config)                                                 │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    │  HTTP POST /api/lessons
                                    │  (proxied by Vite :3000 → :8080)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  LessonController.java  (Spring Boot)                                           │
│                                                                                 │
│  12. @PostMapping receives the request                                          │
│  13. @Valid @RequestBody Lesson lesson → deserializes JSON + validates          │
│      - If validation fails → MethodArgumentNotValidException → 400              │
│      - If valid → calls lessonService.createLesson(lesson)                      │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  LessonService.java                                                             │
│                                                                                 │
│  14. createLesson(lesson)                                                       │
│      - Sets lesson.id = null (ensures DB generates the ID)                      │
│      - Calls repository.save(lesson)                                            │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  LessonRepository.java (Spring Data JPA)                                        │
│                                                                                 │
│  15. save(lesson) → Hibernate generates INSERT INTO lesson (...)                │
│      → H2 in-memory database stores the row                                     │
│      → Returns the entity with generated ID                                     │
│                                                                                 │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    │  ← Response bubbles back up
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  RESPONSE PATH (success)                                                        │
│                                                                                 │
│  16. Controller returns ResponseEntity 201 Created + saved lesson JSON          │
│  17. apiClient.js parses JSON response → returns lesson object                  │
│  18. lessonService.js resolves the Promise with the created lesson              │
│  19. useLessons:                                                                │
│      - dispatch({ type: MUTATE_SUCCESS }) → saving=false                        │
│      - Calls loadLessons() to refetch the full list (GET /api/lessons)          │
│      - dispatch(FETCH_SUCCESS) updates lessons[] in state                       │
│  20. LessonDashboard: setShowForm(false) → hides the modal                      │
│  21. React re-renders LessonList with the new lesson visible                    │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│  ERROR PATH (if something fails)                                                │
│                                                                                 │
│  • Network error    → apiClient throws ApiError (status 0)                      │
│  • Validation (400) → apiClient parses { message } from backend, throws ApiError│
│  • Server error     → apiClient throws ApiError (status 500)                    │
│                                                                                 │
│  • useLessons catches the error:                                                │
│    dispatch({ type: MUTATE_ERROR, payload: err.message })                       │
│    → saving=false, error="..." in state                                         │
│                                                                                 │
│  • LessonDashboard renders <ErrorMessage> with retry button                     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Summary of Components in the Flow

| Step | Layer | File | Role |
|------|-------|------|------|
| 1–3 | UI (Container) | `LessonDashboard.jsx` | Manages form state, passes callbacks |
| 4–6 | UI (Presentational) | `LessonForm.jsx` | Collects input, validates, calls onSubmit |
| 7–9 | Hook (Logic) | `useLessons.js` | Manages async state via useReducer, delegates to service |
| 10 | Service | `lessonService.js` | Maps domain action → HTTP verb + endpoint |
| 11 | HTTP Client | `apiClient.js` | Handles fetch, JSON, headers, error parsing |
| 12–13 | Controller | `LessonController.java` | Routes HTTP request, triggers validation |
| 14 | Service | `LessonService.java` | Business logic (nulls ID, calls repo) |
| 15 | Repository | `LessonRepository.java` | JPA → SQL INSERT via Hibernate → H2 |

---

## Roadmap

- [x] Week 5: React dashboard with hooks, search, filter
- [x] Week 6: Spring Boot REST API + full CRUD integration
- [ ] Week 7: JWT authentication + role-based access
- [ ] Week 8: Production practices (logging, validation, error handling)
