---
inclusion: always
---

# Security Standards

## Secrets & Credentials

- **Never hardcode passwords, API keys, JWT secrets, or any credentials** in source code, configuration files committed to version control, or frontend code.
- Use environment variables for all sensitive configuration (JWT secret, database passwords, API keys).
- The `application.properties` file must reference environment variables (e.g., `${JWT_SECRET}`) rather than containing literal secret values.
- Default/seed passwords in `DataSeeder.java` or `data.sql` are only acceptable for local development and must be clearly marked as such with comments. Production deployments must override these via environment variables.
- Never log passwords, tokens, secrets, or any sensitive data — not even at DEBUG level.

## Password Handling

- All passwords must be hashed using BCrypt (via Spring Security's `PasswordEncoder`) before storage. No exceptions.
- Never transmit passwords in URL query parameters — always use request bodies over HTTPS.
- Never return password hashes in API responses. User objects sent to the client must exclude the password field.
- Frontend must never store plain-text passwords in localStorage, sessionStorage, cookies, or application state beyond the immediate login form submission.

## Token Security

- JWT tokens are the only credential stored client-side (in localStorage).
- Tokens must have a reasonable expiration time (not excessively long).
- The JWT signing secret must be at least 256 bits and sourced from an environment variable.
- Never include sensitive data (passwords, personal info) in JWT token claims — only include username, role, and expiration.
- On logout, tokens must be cleared from client storage immediately.
- On 401 responses, tokens must be cleared automatically (handled by `apiClient.js`).

## Data in Transit

- All API communication must happen over HTTPS in production.
- Sensitive data (credentials, tokens) must only travel in request/response bodies or Authorization headers — never in URLs or query strings.
- CORS must be configured restrictively in production (not `*`).

## Data at Rest

- Database credentials must use environment variables in production.
- H2 in-memory database is acceptable only for development. Production must use a properly secured database.
- No sensitive data should be stored unencrypted in the database (passwords must always be BCrypt hashed).

## Frontend Security

- Never expose secrets, API keys, or internal configuration in frontend JavaScript bundles.
- Sanitize all user input before rendering to prevent XSS attacks.
- Do not trust client-side role checks for security — they are UX only. All authorization must be enforced server-side.
- Never include credentials or tokens in browser console logs or error messages shown to users.

## Backend Security

- All endpoints handling sensitive data must require authentication (JWT).
- Role-based access control must be enforced at the controller/security config level, not just in the frontend.
- Input validation (`@Valid`) must be applied on all endpoints accepting user data.
- Error responses must never leak internal details (stack traces, SQL queries, internal paths) to the client.
- Use parameterized queries (JPA handles this) — never concatenate user input into queries.
- Rate limiting should be applied to authentication endpoints to prevent brute-force attacks.

## Version Control

- `.gitignore` must exclude files containing secrets (`.env`, `application-prod.properties`, key files).
- Never commit `.env` files, private keys, or certificates to the repository.
- If a secret is accidentally committed, rotate it immediately — removing it from history is not sufficient.

## Code Review Checklist (Security)

Before any code is merged or accepted, verify:
1. No hardcoded secrets or credentials anywhere in the diff
2. Passwords are hashed, never stored or transmitted in plain text
3. JWT claims contain only non-sensitive identifiers
4. API responses do not include password fields or internal errors
5. All new endpoints have appropriate authentication and authorization
6. User input is validated on both frontend and backend
7. No sensitive data appears in logs or console output
