# Feature Specification: 006-reporting-operations

**Feature Branch:** `006-reporting-operations`
**Created:** 2026-06-23
**Updated:** 2026-07-14
**Status:** APPROVED
**Priority:** HIGH
**Specification Level:** Formal Specification

---

# 1. Business Context & Goals

## Business Context
The hotel business needs reliable operational reporting to monitor financial performance, room utilization, and guest feedback. Management and administrators require trustworthy dashboards and exportable reports to support decision-making, audits, and customer experience improvements. Because reporting data impacts executive decisions and compliance visibility, this feature is considered a core operational module.

## Goals
The system shall:
- Provide revenue and occupancy reports for configurable date ranges.
- Support Excel export for offline analysis and auditing.
- Enable review submission and moderation workflows.
- Preserve an immutable audit trail for report access and review moderation actions.
- Enforce role-based access so sensitive reporting data is only visible to authorized users.

## Success Metrics
- 100% of report requests are authorized through RBAC.
- Revenue and occupancy reports are generated within 5 seconds for standard ranges.
- 100% of moderation actions are logged with actor, timestamp, and reason.
- 100% of public review listings exclude hidden content.

---

# 2. Stakeholders & User Personas

## Director
A director reviews business performance and needs aggregated financial and occupancy insights.
**Goals:** Analyze revenue trends, compare occupancy rates, and export reports for leadership review.

## Administrator
An administrator manages operational data and review moderation workflows.
**Goals:** Review suspicious or inappropriate content, maintain trust, and ensure reporting integrity.

## Customer
A customer leaves feedback after a completed stay.
**Goals:** Submit reviews easily, view public feedback, and trust that moderation is handled fairly.

---

# 3. User Scenarios (All Paths)

## US-001 — View Revenue and Occupancy Reports
As a Director, I want to view revenue and occupancy reports for a given date range, so I can evaluate business performance.
- **Given** the user has the `DIRECTOR` or `ADMIN` role
- **When** a report request is submitted
- **Then** the system returns aggregated revenue and occupancy data with HTTP 200 OK.

## US-002 — Export Report to Excel
As an Admin or Director, I want to export report data to Excel, so I can share it offline.
- **Given** a valid report request with date range parameters
- **When** the export action is triggered
- **Then** an Excel file is generated and returned with the appropriate content type.

## US-003 — Moderate a Review
As an Administrator, I want to moderate customer reviews, so I can hide inappropriate content.
- **Given** a review exists with status `VISIBLE`
- **When** an admin submits a moderation action
- **Then** the review status is updated to `HIDDEN` and the change is logged.

## US-004 — Submit a Review
As a Customer, I want to submit a review after a completed stay, so I can share my experience.
- **Given** the booking is completed and the customer has not already submitted a review
- **When** a review is submitted
- **Then** the review is saved, the hotel rating is recalculated, and the review becomes visible publicly.

## US-005 — Prevent Unauthorized Access
As a Staff member, I want reporting data to remain protected, so sensitive business information is not exposed.
- **Given** a user without director or admin privileges
- **When** a report endpoint is accessed
- **Then** the request is rejected with HTTP 403 Forbidden.

---

# 4. Acceptance Criteria (EARS — Exhaustive)

### FR-001: Report Access Control
**WHEN** a report endpoint is requested, **THE SYSTEM SHALL** verify the authenticated user role before returning report data.
**WHERE** the user does not have `DIRECTOR` or `ADMIN` privileges, **THE SYSTEM SHALL** reject the request with HTTP 403 Forbidden.

### FR-002: Revenue and Occupancy Aggregation
**WHEN** revenue or occupancy reports are requested, **THE SYSTEM SHALL** aggregate data by the requested date range and return the results in a consistent response structure.

### FR-003: Excel Export
**WHEN** an Excel export is requested, **THE SYSTEM SHALL** generate the report file and return it as a downloadable spreadsheet without exceeding the defined performance threshold.

### FR-004: Review Visibility
**WHEN** reviews are retrieved for public display, **THE SYSTEM SHALL** only return reviews whose status is `VISIBLE`.

### FR-005: Review Submission Rules
**WHEN** a review is submitted, **THE SYSTEM SHALL** validate that the booking is completed and that no prior review exists for the same booking.
**WHERE** validation fails, **THE SYSTEM SHALL** reject the request with HTTP 400 Bad Request.

### FR-006: Review Moderation Audit
**WHEN** a review is moderated, **THE SYSTEM SHALL** persist the action, actor, timestamp, and moderation reason for traceability.

---

# 5. Business Rules
- **BR-001:** Only `DIRECTOR` and `ADMIN` roles may access financial and occupancy reports.
- **BR-002:** Only `ADMIN` users may moderate reviews.
- **BR-003:** A customer may submit at most one review per completed booking.
- **BR-004:** Hidden reviews must not appear in public listing APIs or hotel detail responses.
- **BR-005:** Review moderation actions must be immutable and auditable.
- **BR-006:** Report generation must use the same business date range semantics across all reporting endpoints.
- **BR-007:** Exported reports must reflect the latest committed booking and review data available at request time.

---

# 6. API Contracts (Key Endpoints)

### Revenue Report
```http
GET /api/reports/revenue
```
- **Query Params:** `startDate`, `endDate`, optional `hotelId`
- **Response 200:** Aggregated revenue data by day or month.

### Occupancy Report
```http
GET /api/reports/occupancy
```
- **Query Params:** `startDate`, `endDate`, optional `hotelId`
- **Response 200:** Occupancy summary and room utilization metrics.

### Excel Export
```http
GET /api/reports/export
```
- **Query Params:** `reportType`, `startDate`, `endDate`
- **Response 200:** Excel file stream with `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` content type.

### Submit Review
```http
POST /api/reviews
```
- **Request:** `{ "bookingId": "uuid", "hotelId": "uuid", "rating": 5, "comment": "Great stay" }`
- **Response 201:** Created review record.

### Moderate Review
```http
PATCH /api/reviews/{reviewId}/moderate
```
- **Request:** `{ "action": "HIDE", "reason": "Inappropriate content" }`
- **Response 200:** Updated review record.

---

# 7. Data Models & DB Schema

## Entity: ReportSnapshot
| Field | Type | Description |
|---|---|---|
| report_id | BIGINT | Primary Key |
| report_type | VARCHAR | REVENUE / OCCUPANCY / EXPORT |
| start_date | DATE | Report start date |
| end_date | DATE | Report end date |
| generated_by | BIGINT | User who requested the report |
| generated_at | TIMESTAMP | Creation timestamp |
| payload | TEXT | Aggregated report data |

## Entity: Review
| Field | Type | Description |
|---|---|---|
| review_id | BIGINT | Primary Key |
| booking_id | BIGINT | Foreign Key |
| user_id | BIGINT | Reviewer |
| hotel_id | BIGINT | Target hotel |
| rating | INT | 1–5 |
| comment | TEXT | Review text |
| status | VARCHAR | VISIBLE / HIDDEN |
| moderated_by | BIGINT | Admin who moderated |
| moderated_at | TIMESTAMP | Moderation timestamp |
| moderation_reason | VARCHAR | Reason for moderation |

## Entity: ReviewModerationAudit
| Field | Type | Description |
|---|---|---|
| audit_id | BIGINT | Primary Key |
| review_id | BIGINT | Target review |
| action | VARCHAR | HIDE / REINSTATE |
| actor_id | BIGINT | Admin user |
| action_time | TIMESTAMP | Audit timestamp |
| reason | TEXT | Audit reason |

---

# 8. Error Handling Matrix

| Error Condition | HTTP Status | Mitigation / Retry |
|---|---|---|
| Unauthorized report access | 403 Forbidden | Reject request and log access attempt |
| Invalid date range | 400 Bad Request | Return validation message |
| Review submission for incomplete booking | 400 Bad Request | Reject and explain policy |
| Duplicate review for same booking | 409 Conflict | Reject without creating duplicate |
| Export generation failure | 500 Internal Error | Retry once and alert operations |
| Moderation action without admin role | 403 Forbidden | Reject and log security event |

---

# 9. Non-Functional Requirements

## Security & Reliability
- All report endpoints must require authentication and RBAC authorization.
- Sensitive financial data must never be exposed to unauthorized users.
- Review moderation actions must be logged for auditability.
- Reporting queries must run against transactional data to avoid stale or partial results.

## Performance
- Revenue and occupancy report generation must complete within 5 seconds for standard date ranges.
- Excel export must complete within 10 seconds for typical datasets.
- Public review listing endpoints must return results quickly and exclude hidden reviews by default.

---

# 10. Rollout Plan & Dependencies
- **Dependencies:** Booking Module, Hotel Catalog Module, Authentication Module, Notification Module.
- **Rollout Phase 1:** Deploy report endpoints and role-based access controls in staging.
- **Rollout Phase 2:** Enable Excel export and review moderation workflows with test data.
- **Rollout Phase 3:** Roll out to production and monitor report response times and audit log volume.

