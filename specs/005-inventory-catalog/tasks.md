# Tasks: 005-inventory-catalog

**Input**: Design documents from `/specs/005-inventory-catalog/`

## Phase 1: Hotel CRUD Management
- [x] T001 Implement write methods (`save`, `update`, `delete`) in `HotelServiceImpl`
- [x] T002 In `HotelServiceImpl`, check for existing bookings before allowing soft-deletions
- [x] T003 Expose POST, PUT, DELETE mappings on `/api/v1/hotels` in `HotelController` securing them with `@PreAuthorize`
- [x] T004 Write unit tests verifying delete restrictions and update mechanisms

## Phase 2: Room CRUD & Image Uploads
- [x] T005 Implement write methods in `RoomServiceImpl` (add, edit, delete room types)
- [x] T006 Implement multi-part file upload support in `HotelController` to attach `HotelImage` entries
- [x] T007 Expose POST, PUT, DELETE mappings on `/api/v1/rooms` in `RoomController`
- [x] T008 Write unit tests in `RoomServiceImplTest` verifying room deletions and constraints
