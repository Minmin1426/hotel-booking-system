# Implementation Plan: 003-booking-management

**Branch**: `003-booking-management` | **Date**: 2026-06-23 | **Spec**: [spec.md](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/specs/003-booking-management/spec.md)

## Summary
Implement booking creation, date validation, and room locking logic. Setup a scheduler to release expired locks and clean up database records.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Scheduling, Spring Data JPA
- **Storage**: SQL Server
- **Testing**: JUnit 5, Mockito, Spring Security Test

## Constitution Check
- **Feature Package**: `com.hotelbooking.booking`, `com.hotelbooking.room` (RoomLock), `com.hotelbooking.setting`
- **Dependency Isolation**: `BookingServiceImpl` depends on `RoomLockService` and `SystemSettingService`.
- **DTOs**: Return `BookingResponse`, accept `BookingRequest`.
- **Security**: JWT-based security on `/api/v1/bookings/**` (excluding `/api/v1/rooms/search` and `/api/v1/bookings/validate-dates`).
- **Error Handling**: Custom exceptions (`BusinessException`, `ResourceNotFoundException`) mapped in [GlobalExceptionHandler.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/common/exception/GlobalExceptionHandler.java).

## Project Structure

### Source Code
- `src/main/java/com/hotelbooking/booking/`
  - [BookingController.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/booking/BookingController.java)
  - [BookingService.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/booking/BookingService.java)
  - [BookingServiceImpl.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/booking/BookingServiceImpl.java)
  - [BookingRepository.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/booking/BookingRepository.java)
- `src/main/java/com/hotelbooking/room/`
  - [RoomLock.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomLock.java)
  - [RoomLockRepository.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomLockRepository.java)
  - [RoomLockService.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomLockService.java)
  - [RoomLockServiceImpl.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomLockServiceImpl.java)
  - [RoomLockCleanupScheduler.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomLockCleanupScheduler.java)
- `src/main/java/com/hotelbooking/setting/`
  - [SystemSettingService.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/setting/SystemSettingService.java)
  - [SystemSettingServiceImpl.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/setting/SystemSettingServiceImpl.java)

### Testing
- `src/test/java/com/hotelbooking/`
  - [BookingServiceImplTest.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/booking/BookingServiceImplTest.java)
  - [RoomLockServiceImplTest.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/room/RoomLockServiceImplTest.java)
  - [RoomLockCleanupSchedulerTest.java](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/room/RoomLockCleanupSchedulerTest.java)
