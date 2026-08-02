# Tasks: 006-reporting-operations

**Input**: Design documents from `/specs/006-reporting-operations/`

## Phase 1: Business Reports & Excel Generation
- [x] T001 Define custom aggregation queries in `BookingRepository` and `PaymentRepository`
- [x] T002 Implement reporting business logic in `ReportServiceImpl` to compile occupancy rates and revenue charts
- [x] T003 Expose GET `/api/v1/reports/revenue` and GET `/api/v1/reports/occupancy` in `ReportController` securing them with `@PreAuthorize`
- [x] T004 Implement Excel spreadsheet generation logic using Apache POI
- [x] T005 Expose GET `/api/v1/reports/export` returning file stream download
- [x] T006 Write unit tests in `ReportServiceImplTest` verifying report logic

## Phase 2: Review Moderation
- [x] T007 Define `Review` JPA entity and `ReviewRepository` under `com.hotelbooking.hotel`
- [x] T008 Implement review submission validation rules (users must have completed bookings for the hotel)
- [x] T009 Implement admin censor action in `ReviewService`
- [x] T010 Expose censor endpoint PUT `/api/v1/admin/reviews/{id}/censor` and GET reviews for hotels
