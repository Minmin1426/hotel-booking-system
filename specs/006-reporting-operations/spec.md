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
As an Admin, I want to moderate customer reviews, so I can hide reviews that violate community guidelines.

**Why this priority**: Essential to keep public listings clean and trustworthy.
**Independent Test**: Hide a review using PATCH `/api/v1/reports/reviews/{id}/moderate` (ModerationRequest with action="HIDE") and verify it no longer displays on the hotel detail page.

**Acceptance Scenarios**:
1. **Given** a review violating guidelines, **When** hidden by Admin, **Then** the status is saved as `HIDDEN` and public listings filter it out.

### User Story 4 - Submit Reviews (Priority: P2)
As a Customer, I want to submit a star rating and comment for a hotel stay, so I can share my feedback.

**Independent Test**: Submit a review via POST `/api/v1/reviews` for a completed booking and check average rating.

**Acceptance Scenarios**:
1. **Given** a completed booking, **When** submitting a review, **Then** the review is saved with status `VISIBLE` and hotel's average rating is recalculated.
2. **Given** an uncompleted booking, **When** attempting to submit a review, **Then** the request is rejected.

## Requirements

### Functional Requirements
- **FR-001**: Only users with role `DIRECTOR` or `ADMIN` can retrieve revenue and occupancy statistics.
- **FR-002**: Excel exports must run efficiently and streams the output directly to avoid memory overflows.
- **FR-003**: Customer review listings must only return reviews that are active (`status = 'VISIBLE'`).
- **FR-004**: Reviews are allowed only once per completed booking.

### Key Entities
- **Review**: Represents user comments and star rating. Fields: `reviewId`, `bookingId`, `userId`, `hotelId`, `rating`, `comment`, `status` (VISIBLE, HIDDEN), `moderatedBy`, `moderatedAt`, `moderationReason`.

## Success Criteria
- **SC-001**: Only authorized users (Admin, Director) can access report data.
- **SC-002**: Excel export completes in less than 5 seconds.
- **SC-003**: Average hotel rating dynamically updates upon review submission or moderation.
