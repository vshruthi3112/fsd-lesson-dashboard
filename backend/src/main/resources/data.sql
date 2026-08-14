-- ============================================
-- SEED LESSONS
-- ============================================
-- Seed data matching the original mock lessons from the React frontend.
-- This runs on every startup since we're using H2 in-memory.
--
-- Default user accounts are created by DataSeeder.java (not SQL),
-- because passwords need BCrypt hashing at runtime.
-- Login with:
--   admin / password123       → ADMIN (full access)
--   instructor / password123  → INSTRUCTOR (view + create only)

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('Introduction to React Hooks', 'Learn the basics of useState and useEffect hooks in React.', 'Frontend', 'Jane Smith', 45, 'Beginner', '2026-06-01');

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('Spring Boot REST APIs', 'Build RESTful services with Spring Boot and JPA.', 'Backend', 'John Doe', 60, 'Intermediate', '2026-06-03');

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('State Management with useReducer', 'Advanced state patterns using useReducer and Context API.', 'Frontend', 'Jane Smith', 50, 'Intermediate', '2026-06-05');

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('JWT Authentication', 'Implement secure authentication using JSON Web Tokens.', 'Security', 'Alex Johnson', 55, 'Advanced', '2026-06-08');

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('Database Design with PostgreSQL', 'Relational database design principles and SQL queries.', 'Backend', 'John Doe', 70, 'Beginner', '2026-06-10');

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('CSS Grid and Flexbox', 'Modern layout techniques for responsive web design.', 'Frontend', 'Maria Garcia', 40, 'Beginner', '2026-06-12');

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('Docker for Developers', 'Containerize your applications with Docker and Docker Compose.', 'DevOps', 'Alex Johnson', 65, 'Intermediate', '2026-06-15');

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('Testing with JUnit and Mockito', 'Write unit and integration tests for Java applications.', 'Backend', 'John Doe', 50, 'Intermediate', '2026-06-18');

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('React Router and Navigation', 'Client-side routing and navigation patterns in React.', 'Frontend', 'Jane Smith', 35, 'Beginner', '2026-06-20');

INSERT INTO lesson (title, description, category, instructor, duration, level, date) VALUES
('CI/CD with GitHub Actions', 'Automate build, test, and deploy pipelines.', 'DevOps', 'Alex Johnson', 55, 'Advanced', '2026-06-22');
