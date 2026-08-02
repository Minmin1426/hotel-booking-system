# Hotel Booking Management System Constitution

This document defines the governing principles, development guidelines, and technical standards for the Hotel Booking Management System. It serves as the project constitution, aligned with the rules defined in `AGENTS.md` and `PROJECTAGENT.md`.

## Core Principles

### I. Specification-Driven Development (SDD)
Every implementation must originate from an approved specification. Specifications are the Single Source of Truth (SSoT) for the project's features, APIs, and data models. AI is a collaborator, not an architect, and must never assume or fabricate business rules.

### II. Package-by-Feature Architecture
The system is organized into self-contained feature packages under `com.hotelbooking` (e.g., `auth`, `user`, `hotel`, `room`, `booking`, `payment`, `voucher`, `report`, `setting`).
- Feature packages must maintain encapsulation.
- Repository and implementation classes should be package-private where possible to prevent unauthorized cross-feature access.
- Circular dependencies between features are strictly prohibited.

### III. Security & Secrets Management (Non-Negotiable)
- **Zero Secrets in Code**: No passwords, keys, connection strings, or SMTP credentials may be hardcoded or checked into Git. Use `${VARIABLE_NAME}` placeholders referencing environment variables.
- **Password Safety**: Passwords must be hashed using BCrypt (`BCryptPasswordEncoder` with strength >= 12). Plaintext passwords must never be stored, logged, or returned.
- **Authentication**: JWTs must expire in <= 24 hours. Refresh token rotation is required.
- **CORS & CSRF**: Restrict CORS to trusted origins (no `*` in production). Enable CSRF protection for session-based endpoints, disabling it only for stateless JWT APIs.

### IV. Layer Separation within Features
Each feature package follows a strict internal layer structure: `controller -> service -> repository`.
- **Controller**: Only handles HTTP requests, input validation, and maps DTOs. Zero business logic is allowed.
- **Service**: Implements business logic and coordinates transaction boundaries. Zero HTTP handling.
- **Repository**: Handles database queries, filtering, and persistence.
- **DTOs**: Mandated for all REST request and response payloads. Database entities must never be exposed directly to the REST layer.

### V. Constructor Injection
All dependency injections must use constructor-based injection. Field injection (e.g., `@Autowired private UserService userService;`) is strictly forbidden. Use Lombok's `@RequiredArgsConstructor` or explicit constructors.

### VI. Robust Testing Standards
Every feature must be verified using unit and integration tests under `src/test/java/com/hotelbooking/`.
- Run `mvn test` before committing.
- Run `mvn dependency:check` (OWASP dependency check plugin) before releases to ensure zero critical CVE dependencies.
- TDD is encouraged: define validation scenarios and tests before implementation.

### VII. Consistent Error Handling
All API errors must be intercepted by a global `@ControllerAdvice` (`GlobalExceptionHandler`) and return a standardized JSON response:
```json
{
  "timestamp": "2026-06-23T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Check-out date must be after check-in date",
  "path": "/api/bookings"
}
```
Stack traces, raw SQL error messages, or internal class names must never be exposed in API responses.

## Technical Context & Constraints
- **Language/Version**: Java 17 LTS
- **Framework**: Spring Boot 3 / 4, Spring Security, Spring Data JPA (Hibernate)
- **Database**: Microsoft SQL Server (All DDL modifications via Flyway migrations only)
- **Security**: Stateless JWT Authentication, BCrypt Hashing
- **Build Tool**: Maven

## Governance & Escalation
- **AGENTS.md** security rules supersede all other conventions.
- If conflicts arise, AI agents must halt work and escalate to the team lead via the team chat or GitHub issue tagged `security`.
- Do not commit directly to `main`. All changes require a Pull Request and human review.

---
**Version**: 3.0.0 | **Ratified**: 2026-05-26 | **Last Amended**: 2026-06-23
