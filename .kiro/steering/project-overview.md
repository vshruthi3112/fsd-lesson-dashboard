---
inclusion: always
---

# Lesson Dashboard - Project Overview

## Purpose
This is a learning project following a Full Stack Development plan (Month 2).
The goal is to build a Lesson Dashboard app that evolves week by week:

- **Week 5** ✅: React Refresher — Hooks, state management, search & filter
- **Week 6** ✅ (current): Frontend connected to Spring Boot backend (CRUD via REST API)
- **Week 7**: JWT Authentication with role-based authorization
- **Week 8**: Production practices (logging, validation, exception handling)

## Architecture

```
src/                              # React Frontend (Vite, port 3000)
├── components/                   # React UI components (presentational + container)
│   ├── LessonDashboard.jsx       # Main orchestrator (composes hooks)
│   ├── LessonForm.jsx            # Create/Edit form (modal overlay)
│   ├── LessonList.jsx            # Renders filtered lesson cards
│   ├── LessonCard.jsx            # Single lesson display
│   ├── SearchBar.jsx             # Text search input
│   ├── FilterPanel.jsx           # Category/level dropdowns
│   ├── LoadingSpinner.jsx        # Loading state UI
│   └── ErrorMessage.jsx          # Error banner with retry
├── hooks/                        # Custom hooks (business logic, state)
│   ├── useLessons.js             # CRUD operations + loading/saving/error state
│   ├── useSearch.js              # Text search with useMemo
│   └── useFilter.js              # Multi-criteria filtering
├── services/                     # Data access layer (HTTP abstraction)
│   ├── apiClient.js              # Centralized fetch wrapper (error handling, JSON)
│   └── lessonService.js          # Lesson CRUD endpoints
└── styles/
    └── index.css                 # Plain CSS styles

backend/                          # Spring Boot Backend (port 8080)
├── controller/
│   └── LessonController.java    # REST endpoints (GET/POST/PUT/DELETE /api/lessons)
├── service/
│   └── LessonService.java       # Business logic layer
├── repository/
│   └── LessonRepository.java    # Spring Data JPA interface
├── model/
│   └── Lesson.java              # JPA entity with validation annotations
├── exception/
│   └── GlobalExceptionHandler.java  # Centralized error responses
└── resources/
    ├── application.properties    # H2 config, server settings
    └── data.sql                  # Seed data
```

## Key Design Decisions

1. **Service Layer Abstraction**: Components never call APIs directly. They use
   custom hooks, which use the service layer (`apiClient.js` → `lessonService.js`).
   The apiClient handles base URL, JSON serialization, and error normalization.

2. **Custom Hooks for Logic**: All data fetching, search, and filter logic lives
   in hooks — not in components. Components are purely for rendering.

3. **Composable Hooks**: `useLessons`, `useSearch`, and `useFilter` are independent
   and composed in `LessonDashboard`. Data flows: lessons → search → filter → render.

4. **useReducer for Complex State**: `useLessons` uses `useReducer` to manage
   loading/saving/error states cleanly across multiple async operations.

5. **Cleanup & Cancellation**: The initial fetch in `useLessons` uses an
   `isCancelled` flag to prevent state updates after unmount.

6. **API Error Handling**: `apiClient.js` provides a custom `ApiError` class,
   parses Spring Boot error responses, and normalizes network failures.

7. **Vite Proxy**: Dev server at :3000 proxies `/api` requests to Spring Boot
   at :8080, avoiding CORS issues in development.

## Tech Stack

- **Frontend**: React 18 (Vite), Plain CSS
- **Backend**: Spring Boot 3, Spring Data JPA, H2 (in-memory DB)
- **Dev Tooling**: Vite proxy, hot reload
- **Coming**: JWT auth (Week 7), logging/validation hardening (Week 8)

## Running the App

1. **Backend**: `cd backend && ./mvnw spring-boot:run` (starts on :8080)
2. **Frontend**: `npm run dev` (starts on :3000, proxies /api to :8080)
