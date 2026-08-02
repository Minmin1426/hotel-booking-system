-- V5__Add_room_status_and_hotel_rating.sql
-- Description: Add status column to rooms, rating column to hotels, and indexes for UC-07/08/09 queries

-- 1. Add status column to rooms
ALTER TABLE rooms ADD status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE';

-- 2. Add rating column to hotels
ALTER TABLE hotels ADD rating DECIMAL(3, 2) NULL;

-- 3. Add indexes for optimizing room availability queries
CREATE INDEX idx_rooms_hotel_status ON rooms(hotel_id, status);

-- 4. Add index on booking dates for range queries
CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);

-- 5. Add index on booking rooms for quick lookup by booking id
CREATE INDEX idx_booking_rooms_booking_id ON booking_rooms(booking_id);

-- 6. Add index on hotel rating for sorting
CREATE INDEX idx_hotels_rating ON hotels(rating);
