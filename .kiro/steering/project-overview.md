---
inclusion: always
---

# Lesson Dashboard - Project Overview

## Purpose
This is a learning project following a Full Stack Development plan (Month 2).
The goal is to build a Lesson Dashboard app that evolves week by week:

- **Week 5** ✅: React Refresher — Hooks, state management, search & filter
- **Week 6** ✅: Frontend connected to Spring Boot backend (CRUD via REST API)
- **Week 7** ✅ (current): JWT Authentication with role-based authorization
- **Week 8**: Production practices (logging, validation, exception handling)

## Architecture

```
src/                              # React Frontend (Vite, port 3000)
├── components/                   # React UI components (presentational + container)
│   ├── LessonDashboard.jsx       # Main orchestrator (composes hooks, role-based UI)
│   ├── LessonForm.jsx            # Create/Edit form (modal overlay)
│   ├── LessonList.jsx            # Renders filtered lesson cards
│   ├── LessonCard.jsx            # Single lesson display (conditionally shows edit/delete)
│   ├── LoginForm.jsx             # Login/Register form with role selection
│   ├── ConfirmModal.jsx          # Reusable confirmation dialog (native <dialog>)
│   ├── SearchBar.jsx             # Text search input
│   ├── FilterPanel.jsx           # Category/level dropdowns
│   ├── LoadingSpinner.jsx        # Loading state UI
│   └── ErrorMessage.jsx          # Error banner with retry
├── hooks/                        # Custom hooks (business logic, state)
│   ├── useLessons.js             # CRUD operations + loading/saving/error state
│   ├── useAuth.js                # Authentication state (login/logout/register)
│   ├── useSearch.js              # Text search with useMemo
│   └── useFilter.js              # Multi-criteria filtering
├── services/                     # Data access layer (HTTP abstraction)
│   ├── apiClient.js              # Centralized fetch wrapper (attaches JWT token)
│   ├── authService.js            # Login/register/logout API calls
│   ├── tokenStorage.js           # localStorage wrapper for JWT token + user info
│   └── lessonService.js          # Lesson CRUD endpoints
└── styles/
    └── index.css                 # Plain CSS styles (includes auth styles)

backend/                          # Spring Boot Backend (port 8080)
├── controller/
│   ├── LessonController.java    # REST endpoints (GET/POST/PUT/DELETE /api/lessons)
│   └── AuthController.java      # Auth endpoints (POST /api/auth/login, /register)
├── service/
│   ├── LessonService.java       # Business logic layer
│   └── AuthService.java         # Login/register logic (credential validation, token generation)
├── repository/
│   ├── LessonRepository.java    # Spring Data JPA interface
│   └── UserRepository.java      # User lookup (findByUsername)
├── model/
│   ├── Lesson.java              # JPA entity with validation annotations
│   ├── User.java                # User entity (username, password hash, role)
│   └── Role.java                # Enum: ADMIN, INSTRUCTOR
├── security/
│   ├── SecurityConfig.java      # Endpoint authorization rules + BCrypt bean
│   ├── JwtUtil.java             # JWT token creation + validation utility
│   ├── JwtFilter.java           # Request filter — validates token on every request
│   ├── CustomUserDetailsService.java  # Connects Spring Security to our User table
│   └── DataSeeder.java          # Creates default users on startup
├── exception/
│   └── GlobalExceptionHandler.java  # Centralized error responses
└── resources/
    ├── application.properties    # H2 config, server settings, JWT config
    └── data.sql                  # Seed data (lessons)
```

## Key Design Decisions

1. **Service Layer Abstraction**: Components never call APIs directly. They use
   custom hooks, which use the service layer (`apiClient.js` → `lessonService.js`).
   The apiClient handles base URL, JSON serialization, error normalization, and JWT token attachment.

2. **Custom Hooks for Logic**: All data fetching, search, filter, and auth logic lives
   in hooks — not in components. Components are purely for rendering.

3. **Composable Hooks**: `useLessons`, `useSearch`, `useFilter`, and `useAuth` are independent
   and composed in parent components. Data flows: auth → lessons → search → filter → render.

4. **useReducer for Complex State**: `useLessons` uses `useReducer` to manage
   loading/saving/error states cleanly across multiple async operations.

5. **Cleanup & Cancellation**: The initial fetch in `useLessons` uses an
   `isCancelled` flag to prevent state updates after unmount.

6. **API Error Handling**: `apiClient.js` provides a custom `ApiError` class,
   parses Spring Boot error responses, normalizes network failures, and auto-clears
   auth on 401 responses.

7. **Vite Proxy**: Dev server at :3000 proxies `/api` requests to Spring Boot
   at :8080, avoiding CORS issues in development.

8. **JWT Stateless Auth**: No server-side sessions. The JWT token carries all identity
   info (username, role, expiration). Each request is self-contained.

9. **Role-Based Authorization**: Backend enforces roles at the endpoint level via
   SecurityConfig. Frontend hides/shows UI elements based on role for better UX.

10. **Token Storage Separation**: `tokenStorage.js` is a shared module used by both
    `apiClient.js` (reads token) and `authService.js` (writes token), avoiding circular imports.

## Authentication & Authorization

- **Roles**: ADMIN (full CRUD), INSTRUCTOR (view + create only)
- **JWT Token**: Stored in localStorage, attached to every request via Authorization header
- **Protected Endpoints**: All `/api/lessons` endpoints require authentication
- **Role Enforcement**: PUT/DELETE restricted to ADMIN; POST allowed for both roles
- **Default Accounts**: admin/password123 (ADMIN), instructor/password123 (INSTRUCTOR)

## Tech Stack

- **Frontend**: React 18 (Vite), Plain CSS
- **Backend**: Spring Boot 3, Spring Data JPA, H2 (in-memory DB)
- **Security**: Spring Security, JWT (JJWT library), BCrypt password hashing
- **Dev Tooling**: Vite proxy, hot reload
- **Coming**: Logging/validation hardening (Week 8)

## Running the App

1. **Backend**: `cd backend && mvn spring-boot:run` (starts on :8080)
2. **Frontend**: `npm run dev` (starts on :3000, proxies /api to :8080)
3. **Login**: Use admin/password123 or instructor/password123
