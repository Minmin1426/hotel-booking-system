# Implementation Plan: 005-inventory-catalog

**Branch**: `005-inventory-catalog` | **Date**: 2026-06-23 | **Spec**: [spec.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/005-inventory-catalog/spec.md)

## Summary
Build inventory CRUD services for hotels, rooms, and image attachments. Configure Spring Security endpoints with `@PreAuthorize("hasRole('ADMIN')")`.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Security, Spring Data JPA
- **Storage**: SQL Server
- **Testing**: JUnit 5, Mockito

## Constitution Check
- **Feature Package**: `com.hotelbooking.hotel`, `com.hotelbooking.room`
- **Security Check**: Restrict modify endpoints to `ROLE_ADMIN` and `ROLE_STAFF`.
- **DTOs**: Return DTO mappings (`HotelResponse`, `RoomResponse`) rather than entities.

## Project Structure

### Source Code
- `src/main/java/com/hotelbooking/hotel/`
  - [HotelController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/HotelController.java)
  - [HotelService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/HotelService.java)
  - [HotelServiceImpl.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/HotelServiceImpl.java)
  - [HotelImage.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/HotelImage.java)
- `src/main/java/com/hotelbooking/room/`
  - [RoomController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomController.java)
  - [RoomService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomService.java)
  - [RoomServiceImpl.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomServiceImpl.java)

### Testing
- `src/test/java/com/hotelbooking/`
  - [HotelServiceImplTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/service/HotelServiceImplTest.java)
  - [RoomServiceImplTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/service/RoomServiceImplTest.java)
