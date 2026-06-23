# PROJECTAGENT.md
# Project Constitution & AI Collaboration Memory

> Version: 3.0.0  
> Architecture: SDD + ADD  
> Stack: Spring Boot 3 + SQL Server + JWT + JPA  
> Auther: VudtlHE190770
> Last Updated: 2026-05-26

---

# 1. Project Identity

## Project Name

Hotel Booking Management System

---

## Project Goal

Xây dựng hệ thống đặt phòng khách sạn hiện đại, bảo mật, scalable và maintainable sử dụng:

- Java 17
- Spring Boot 3
- Spring Security
- JWT Authentication
- Spring Data JPA (Hibernate)
- SQL Server
- RESTful API
- Maven

Frontend future phase:

- React.js

---

# 2. Core Development Philosophy

Dự án áp dụng mô hình:

## SDD + ADD

```text
SDD = Spec-Driven Development
```

---

## 2.1 SDD Principles

Specification là Source of Truth.

SDD áp dụng cho:

- System architecture
- API contracts
- Database schema
- Authentication
- Authorization
- Business rules
- Validation rules
- Security policy
- Payment workflow
- Booking workflow

Mọi implementation phải xuất phát từ specification.

AI không được tự suy diễn business logic.

---

## 2.2 ADD Principles

AI Agent được dùng để:

- CRUD generation
- DTO generation
- Repository generation
- Service implementation
- REST API generation
- Test generation
- Refactoring
- Documentation support
- Boilerplate reduction

AI là collaborator.

AI KHÔNG phải architect.

---

# 3. Governance Hierarchy

Nếu conflict xảy ra:

```text
1. Security rules trong AGENTS.md
2. Human-approved architecture decisions
3. Approved specifications
4. Existing architecture patterns
5. AI-generated suggestion
```

AI phải escalate thay vì assume.

---

# 4. Human vs AI Responsibilities

## 4.1 Human Responsibilities

Human chịu trách nhiệm:

- Architecture decisions
- Security decisions
- Business rules
- Database design
- Production approval
- Code review
- Final validation
- Release approval

---

## 4.2 AI Responsibilities

AI chịu trách nhiệm:

- Productivity enhancement
- Boilerplate generation
- CRUD implementation
- Refactoring support
- Test generation
- Documentation support
- Pattern consistency

---

# 5. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Backend Framework | Spring Boot 3 |
| Security | Spring Security + JWT |
| Database | SQL Server |
| ORM | Spring Data JPA / Hibernate |
| Build Tool | Maven |
| API Style | RESTful API |
| Frontend | React (future phase) |
| Version Control | Git + GitHub |
| AI Tools | Codex |

---

# 6. System Architecture

## 6.1 Architecture Pattern

Hệ thống sử dụng:

- Layered Architecture
- RESTful API Architecture
- Domain-driven separation
- Stateless authentication

---

## 6.2 Standard Flow

```text
Client
   ↓
Controller Layer
   ↓
Service Layer
   ↓
Repository Layer
   ↓
SQL Server
```

---

# 7. Standard Folder Structure

```text
hotel-booking-system/
│
├── docs/
│   ├── specs/
│   ├── api/
│   ├── database/
│   ├── architecture/
│   │   └── adr/
│   ├── prompts/
│   ├── business-rules/
│   └── reports/
│
├── src/
│   ├── main/
│   │   ├── java/com/hotelbooking/
│   │   │
│   │   ├── config/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   ├── security/
│   │   ├── validation/
│   │   ├── mapper/
│   │   ├── exception/
│   │   └── utils/
│   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── db/migration/
│   │
│   └── test/
│
├── frontend/
│
├── AGENTS.md
├── PROJECTAGENT.md
├── README.md
└── pom.xml
```

---

# 8. Layer Responsibilities

## 8.1 Controller Layer

Controller chỉ được:

- Receive HTTP requests
- Validate DTO
- Authenticate request
- Authorize endpoint access
- Call service layer
- Return API response

Controller KHÔNG được:

- Business logic
- Direct database access
- Entity exposure

---

## 8.2 Service Layer

Service chịu trách nhiệm:

- Business logic
- Workflow processing
- Business authorization
- Transaction management
- Validation coordination

Service KHÔNG được:

- HTTP handling
- Response formatting
- Direct SQL writing

---

## 8.3 Repository Layer

Repository chịu trách nhiệm:

- Database access
- Entity persistence
- Query execution
- Pagination
- Filtering

Repository KHÔNG được:

- Business logic
- Security logic
- HTTP handling

---

## 8.4 DTO Layer

DTO chịu trách nhiệm:

- Request validation
- API response standardization
- API contract consistency
- Prevent entity exposure

JPA Entity KHÔNG được expose trực tiếp qua API.

---

# 9. Coding Constitution (MANDATORY)

## Rule 1 — No Hardcoded Secrets

KHÔNG được:

```java
String jwtSecret = "secret123";
```

BẮT BUỘC:

- Environment variables
- Secure config management
- `${VARIABLE_NAME}` placeholders

---

## Rule 2 — Strict Layer Separation

- Controller KHÔNG chứa business logic
- Service KHÔNG chứa HTTP handling
- Repository KHÔNG chứa business logic
- DTO KHÔNG chứa persistence logic

---

## Rule 3 — DTO Mandatory

BẮT BUỘC:

- Request DTO
- Response DTO

KHÔNG được expose:

- JPA Entity
- Internal model
- Sensitive fields

---

## Rule 4 — Validation Required

Mọi input phải validate:

- Null check
- Empty check
- Length check
- Format validation
- Business validation

BẮT BUỘC dùng:

- Jakarta Bean Validation

Ví dụ:

```java
@NotBlank
@Email
@Size
```

---

## Rule 5 — Password Security

Mật khẩu PHẢI:

- Hash bằng BCrypt
- Không lưu plain text
- Không dùng MD5/SHA1

---

## Rule 6 — JWT Security

BẮT BUỘC:

- Access token expiration
- Refresh token rotation
- Stateless authentication
- Secure signing secret

KHÔNG được:

- Infinite token lifetime
- Hardcoded JWT secret

---

## Rule 7 — Clean Code Required

BẮT BUỘC:

- Meaningful naming
- Single Responsibility Principle
- Small focused methods
- No magic numbers
- Readable code

---

## Rule 8 — No Duplicate Code

BẮT BUỘC:

- Shared utilities
- Reusable validation
- Shared constants
- Common exception handling

---

## Rule 9 — Constructor Injection Only

KHÔNG được:

```java
@Autowired
private UserService userService;
```

BẮT BUỘC:

```java
@RequiredArgsConstructor
```

hoặc constructor injection.

---

# 10. Naming Conventions

## Classes

```java
UserService
BookingController
RoomRepository
JwtAuthenticationFilter
```

---

## Methods

```java
createBooking()
findRoomById()
cancelReservation()
authenticateUser()
```

---

## Variables

```java
hotelName
checkInDate
totalPrice
```

---

## Constants

```java
JWT_EXPIRATION
MAX_LOGIN_ATTEMPTS
DEFAULT_PAGE_SIZE
```

---

# 11. Database Standards

## 11.1 Table Naming

```text
users
hotels
rooms
bookings
payments
reviews
```

---

## 11.2 Required Columns

Mọi bảng PHẢI có:

- id
- created_at
- updated_at

---

## 11.3 Required Constraints

Mọi bảng PHẢI có:

- PRIMARY KEY
- FOREIGN KEY
- NOT NULL
- Index cho foreign key
- Reasonable default values

---

## 11.4 Forbidden SQL Practices

KHÔNG được:

- SELECT *
- Dynamic SQL concatenation
- Missing transaction
- Duplicate query logic
- Unindexed foreign key

---

# 12. Security Standards

## 12.1 Authentication

BẮT BUỘC:

- JWT authentication
- BCrypt password hashing
- Secure logout
- Token expiration
- Refresh token rotation

---

## 12.2 Authorization

Authorization phải dùng:

- Spring Security
- JWT filter
- `@PreAuthorize`

BẮT BUỘC chặn:

- Unauthorized access
- Privilege escalation
- URL abuse

---

## 12.3 SQL Injection Prevention

100% database access phải dùng:

- Spring Data JPA
- Parameter binding
- JPQL parameters
- Criteria API

KHÔNG được:

- Query string concatenation

---

## 12.4 XSS Prevention

BẮT BUỘC:

- Output encoding
- Frontend sanitization
- Input validation

---

## 12.5 CSRF

Nếu dùng:

- Cookie authentication
- Session-based frontend

thì CSRF protection là mandatory.

Stateless JWT API phải cấu hình rõ ràng.

---

## 12.6 CORS

KHÔNG được:

```java
allowedOrigins("*")
```

Production chỉ cho trusted frontend domains.

---

# 13. Non-Functional Requirements (NFR)

## 13.1 Performance

API response time target:

- Read API < 500ms
- Write API < 1s
- Search API < 2s

Pagination bắt buộc cho large datasets.

---

## 13.2 Scalability

System phải hỗ trợ:

- Stateless horizontal scaling
- Multiple backend instances
- Database connection pooling

---

## 13.3 Reliability

BẮT BUỘC:

- Transaction rollback on failure
- Proper exception handling
- Graceful error response
- Retry strategy cho external services

---

## 13.4 Maintainability

Code phải:

- Modular
- Reusable
- Testable
- Readable

Technical debt phải documented.

---

## 13.5 Observability

BẮT BUỘC:

- Structured logging
- Error tracking
- Request tracing
- Audit logging cho security actions

---

## 13.6 Availability

Production target:

- No single point of failure
- Graceful shutdown support
- Health check endpoint required

Ví dụ:

```text
/actuator/health
```

---

## 13.7 Security

BẮT BUỘC:

- OWASP-aware practices
- Secret isolation
- Principle of least privilege
- Input validation everywhere

---

## 13.8 Backup & Recovery

BẮT BUỘC:

- Database backup strategy
- Recovery procedure documentation
- Migration rollback support

---

## 13.9 API Quality

API phải:

- Consistent
- Versioned
- Backward-compatible khi có thể
- Properly documented

---

## 13.10 Testing Standards

BẮT BUỘC:

- Unit test cho service layer
- Validation test
- Security test cho auth endpoints
- Integration test cho critical workflows

---

# 14. Exception Architecture

```text
GlobalExceptionHandler
 ├── ValidationException
 ├── AuthenticationException
 ├── AuthorizationException
 ├── ResourceNotFoundException
 ├── BusinessException
 └── DatabaseException
```

---

# 15. Logging Standards

## INFO

- Login success
- Booking created
- Payment completed

---

## WARN

- Validation failure
- Unauthorized access
- Suspicious activity

---

## ERROR

- Database failure
- Payment exception
- System exception

---

## Forbidden Commit Content

KHÔNG được commit:

- Debug code
- System.out.println
- Hardcoded secrets
- Test credentials
- Temporary logs

---

# 16. API Standards

BẮT BUỘC:

- RESTful naming
- Consistent response structure
- Proper HTTP status code
- Pagination support
- Versioning support

Ví dụ:

```text
/api/v1/bookings
/api/v1/hotels
```

---

# 17. AI Agent Governance

## 17.1 Allowed Tasks

AI Agent ĐƯỢC phép:

- Generate CRUD
- Generate DTO
- Generate Repository
- Generate Service
- Generate Controller
- Generate Unit Test
- Refactor code
- Generate Documentation

---

## 17.2 Restricted Tasks

AI Agent KHÔNG được:

- Disable Spring Security
- Bypass JWT validation
- Modify architecture tự động
- Change business rule tự động
- Add dependency tự động
- Expose entities directly
- Remove validation
- Hardcode secrets

---

## 17.3 Prompt Standard

Mọi prompt nên có:

```text
1. Context
2. Task
3. Constraints
4. Acceptance Criteria
5. Output Format
```

---

# 18. Development Workflow

## Phase 1 — Specification

Trước khi code PHẢI có:

- User story
- Acceptance criteria
- Security requirement
- API requirement
- Database impact
- Edge cases

---

## Phase 2 — Planning

AI Agent phải phân tích:

- Files cần tạo
- Files cần sửa
- Dependency impact
- Security impact
- Database impact

---

## Phase 3 — Implementation

Thứ tự chuẩn:

```text
1. Database migration
2. Entity model
3. DTO
4. Repository
5. Service
6. Controller
7. Security
8. Testing
9. Frontend integration
```

---

## Phase 4 — Validation

Checklist:

- Compile success
- No runtime error
- No SQL injection
- JWT working
- Validation OK
- Logging OK
- Security OK
- API contract OK

---

# 19. Review Checklist

## Logic Review

- Business logic đúng?
- Edge cases xử lý đủ?
- Null handling đầy đủ?

---

## Security Review

- SQL Injection?
- JWT issue?
- Authorization issue?
- Secret leak?
- Password hashing đúng?

---

## Database Review

- Query optimized?
- Transaction handling đúng?
- Constraint đúng?
- Index đúng?

---

## API Review

- RESTful?
- Validation đầy đủ?
- Error response consistent?
- DTO usage đúng?

---

## Maintainability Review

- Naming chuẩn?
- Layer separation đúng?
- Reusable code?
- Duplicate code?
- Readability tốt?

---

# 20. Consistency Gates

Before merge:

- API response phải match spec
- DTO fields phải consistent với DB constraints
- Validation rules phải match acceptance criteria
- Security annotations phải đúng role matrix
- Naming phải consistent với existing patterns
- Không được duplicate business logic
- Không được bypass service layer

---

# 21. Anti-patterns (STRICTLY FORBIDDEN)

## Không Vibe Coding

KHÔNG prompt:

```text
Build full hotel booking system.
```

---

## Không generate toàn bộ project một lần

Phải chia module:

- Authentication
- Hotel
- Room
- Booking
- Payment
- Review
- Report

---

## Không để AI tự quyết architecture

Architecture phải được human approve.

---

## Không merge AI-generated code mà chưa review

Mọi AI-generated code PHẢI:

- Read
- Verify
- Test
- Review

---

# 22. Core Modules

## Authentication Module

- Register
- Login
- Logout
- Refresh token
- Reset password
- Update profile

---

## Hotel Module

- Search hotel
- Filter hotel
- View details
- Upload images

---

## Room Module

- Room availability
- Room pricing
- Room management

---

## Booking Module

- Create booking
- Cancel booking
- Booking history
- Booking status tracking

---

## Payment Module

- Online payment
- Payment history
- Refund handling

---

## Review Module

- Ratings
- Comments
- Moderation

---

## Admin Module

- User management
- Hotel management
- Room management
- Booking management
- Dashboard statistics

---

## Director Module

- Revenue report
- Occupancy report
- Business analytics

---

# 23. Architecture Decision Records (ADR)

## ADR-001 — Layered Architecture

Sử dụng Layered Architecture thay vì Hexagonal Architecture để phù hợp:

- Team size nhỏ
- Deadline đồ án
- Learning curve thấp hơn
- Faster onboarding

---

## ADR-002 — SQL Server

Sử dụng SQL Server vì:

- Requirement của trường
- Strong relational consistency
- Familiar ecosystem

---

## ADR-003 — JWT Authentication

Authentication dùng JWT Stateless Authentication.

Session-based authentication không được dùng cho REST API.

---

# 24. Current Business Rules

## BR-001

User chỉ được review khi có completed booking.

---

## BR-002

Hotel không được hard delete nếu tồn tại booking history.

---

## BR-003

Booking phải validate room availability trước payment.

---

## BR-004

Refund policy phụ thuộc cancellation time.

---

# 25. Current Project Status

## Current Phase

Backend Foundation Setup

---

## Current Priority

- Authentication
- Security
- User management
- JWT infrastructure

---

## Pending Modules

- Payment integration
- Review moderation
- Reporting dashboard

---

# 26. Sprint Structure

| Sprint | Scope |
|---|---|
| Sprint 1 | Authentication + Security |
| Sprint 2 | Hotel + Room |
| Sprint 3 | Booking |
| Sprint 4 | Payment |
| Sprint 5 | Admin Management |
| Sprint 6 | Reporting + Optimization |

---

# 27. Definition of Done (DoD)

Một feature chỉ được xem là hoàn thành khi:

- Compile success
- No runtime error
- Validation OK
- Logging OK
- Security OK
- API response consistent
- No duplicate code
- Architecture compliant
- Specification compliant
- Human reviewed

---

# 28. Final Principles

## Principle 1

Specification là Source of Truth.

---

## Principle 2

AI là collaborator.

KHÔNG phải architect.

---

## Principle 3

Verification quan trọng hơn generation.

---

## Principle 4

Security quan trọng hơn tốc độ phát triển.

---

## Principle 5

Maintainability quan trọng hơn code ngắn.

---

## Principle 6

Code quality luôn ưu tiên hơn tốc độ delivery.

---

# FINAL STATEMENT

Human controls:

- Architecture
- Security
- Business rules
- Final decisions

AI supports:

- Productivity
- Automation
- Refactoring
- Boilerplate reduction
- Documentation
- Testing

AI must obey this document at all times.
