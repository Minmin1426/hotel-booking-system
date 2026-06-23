# Feature Specification: 006-reporting-operations

**Feature Branch**: `006-reporting-operations`
**Created**: 2026-06-23
**Status**: Completed

## User Scenarios & Testing

### User Story 1 - View Revenue and Occupancy Reports (Priority: P1)
As a Director, I want to view revenue reports and room occupancy reports for specified date ranges, so I can analyze business performance.

**Why this priority**: Required for executive decisions and tracking hotel capacity usage.
**Independent Test**: Request GET `/api/v1/reports/revenue?startDate=2026-06-01&endDate=2026-06-30` as a Director and check if data contains total revenue aggregated by day/month.

**Acceptance Scenarios**:
1. **Given** an authenticated Director, **When** requesting a revenue report, **Then** the report is returned with aggregations and HTTP 200 OK.
2. **Given** a Staff member, **When** requesting a revenue report, **Then** access is denied with HTTP 403 Forbidden.

### User Story 2 - Export Excel Reports (Priority: P2)
As an Admin or Director, I want to export reports in Excel format, so I can share them offline.

**Why this priority**: Supports operational workflows and manual audits.
**Independent Test**: Download Excel report from GET `/api/v1/reports/export` and verify the download is successful (response header contains `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`).

**Acceptance Scenarios**:
1. **Given** a requested date range, **When** exporting Excel, **Then** an Excel sheet is generated in <= 5 seconds.

### User Story 3 - Review Moderation (Priority: P2)
As an Admin, I want to moderate customer reviews, so I can hide or delete reviews that violate community guidelines.

**Why this priority**: Essential to keep public listings clean and trustworthy.
**Independent Test**: Hide a review using PUT `/api/v1/admin/reviews/{id}/censor` and verify it no longer displays on the hotel detail page.

**Acceptance Scenarios**:
1. **Given** a review violating guidelines, **When** censored by Admin, **Then** the status is saved and public listings filter it out.

## Requirements

### Functional Requirements
- **FR-001**: Only users with role `DIRECTOR` or `ADMIN` can retrieve revenue and occupancy statistics.
- **FR-002**: Excel exports must run efficiently and streams the output directly to avoid memory overflows.
- **FR-003**: Customer review listings must only return reviews that are approved or not flagged as censored.

### Key Entities
- **Review**: Represents user comments and star rating. Fields: `reviewId`, `bookingId`, `userId`, `hotelId`, `rating`, `comment`, `status` (PENDING, APPROVED, CENSORED).

## Success Criteria
- **SC-001**: Only authorized users (Admin, Director) can access report data.
- **SC-002**: Excel export completes in less than 5 seconds.
