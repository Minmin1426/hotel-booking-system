# Tasks: 002-search-discovery

**Input**: Design documents from `/specs/002-search-discovery/`

## Phase 1: Hotel Discovery
- [x] T001 Define `Hotel` JPA entity and `HotelRepository` with JPQL search queries
- [x] T002 Implement search hotel business logic in `HotelServiceImpl` (filtering active hotels)
- [x] T003 Expose `/api/v1/hotels/search` and GET `/api/v1/hotels/{id}` in `HotelController`
- [x] T004 Write unit tests in `HotelServiceImplTest` verifying search queries and soft-delete conditions

## Phase 2: Room & Availability Checking
- [x] T005 Define `Room` JPA entity and `RoomRepository`
- [x] T006 Implement custom SQL/JPQL in `RoomRepository` to select rooms not having overlapping confirmed bookings or active room locks
- [x] T007 Implement availability checking logic in `RoomServiceImpl`
- [x] T008 Expose `/api/v1/rooms/search` in `RoomController` taking check-in, check-out dates, and hotel ID
- [x] T009 Write unit tests in `RoomServiceImplTest` verifying overlapping dates handling
