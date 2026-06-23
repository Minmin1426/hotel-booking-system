# Implementation Plan: 002-search-discovery

**Branch**: `002-search-discovery` | **Date**: 2026-06-23 | **Spec**: [spec.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/002-search-discovery/spec.md)

## Summary
Implement hotel search, filtering, and room availability check in the REST layer. Ensure queries join bookings and locks tables to calculate real-time availability.

## Technical Context
- **Language/Version**: Java 17
- **Primary Dependencies**: Spring Boot 3.3.0, Spring Data JPA, Hibernate, SQL Server
- **Testing**: JUnit 5, Mockito

## Constitution Check
- **Feature Package**: `com.hotelbooking.hotel`, `com.hotelbooking.room`
- **Separation of Layers**: Controllers call services; services use repository queries.
- **DTOs**: Return `HotelResponse`, `RoomAvailabilityResponse`, etc.

## Project Structure

### Source Code
- `src/main/java/com/hotelbooking/hotel/`
  - [HotelController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/HotelController.java)
  - [HotelService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/HotelService.java)
  - [HotelServiceImpl.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/HotelServiceImpl.java)
  - [HotelRepository.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/hotel/HotelRepository.java)
- `src/main/java/com/hotelbooking/room/`
  - [RoomController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomController.java)
  - [RoomService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomService.java)
  - [RoomServiceImpl.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomServiceImpl.java)
  - [RoomRepository.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/room/RoomRepository.java)

### Testing
- `src/test/java/com/hotelbooking/`
  - [HotelServiceImplTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/service/HotelServiceImplTest.java)
  - [RoomServiceImplTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/service/RoomServiceImplTest.java)
