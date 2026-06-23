# Implementation Plan: 006-reporting-operations

**Branch**: `006-reporting-operations` | **Date**: 2026-06-23 | **Spec**: [spec.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/006-reporting-operations/spec.md)

## Summary
Implement business analytics reports, Excel spreadsheet generation using Apache POI, and administrative review moderation features.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Security, Spring Data JPA
- **Libraries**: Apache POI (for Excel generation)
- **Storage**: SQL Server
- **Testing**: JUnit 5, Mockito

## Constitution Check
- **Feature Package**: `com.hotelbooking.report`, `com.hotelbooking.hotel` (Review)
- **Security Check**: Restrict report retrieval endpoints to `ROLE_DIRECTOR` and `ROLE_ADMIN`.
- **DTOs**: Return DTO summaries like `RevenueReportResponse`, `RoomUsageResponse` instead of entities.

## Project Structure

### Source Code
- `src/main/java/com/hotelbooking/report/`
  - [ReportController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/report/ReportController.java)
  - [ReportService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/report/ReportService.java)
  - [ReportServiceImpl.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/report/ReportServiceImpl.java)
- `src/main/java/com/hotelbooking/hotel/`
  - [Review.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/Review.java)
  - [ReviewRepository.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/ReviewRepository.java)
  - [ReviewController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/ReviewController.java)

### Testing
- `src/test/java/com/hotelbooking/`
  - [ReportServiceImplTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/service/ReportServiceImplTest.java)
