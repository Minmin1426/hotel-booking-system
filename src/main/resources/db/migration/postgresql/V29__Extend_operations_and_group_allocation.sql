-- V29__Extend_operations_and_group_allocation.sql (PostgreSQL)

ALTER TABLE rooms ADD COLUMN IF NOT EXISTS room_type VARCHAR(50) DEFAULT 'STANDARD';
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS max_occupancy INT DEFAULT 2;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS price_per_night DECIMAL(18,2) DEFAULT 1000000.00;

CREATE TABLE IF NOT EXISTS room_services (
    service_id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT REFERENCES hotels(hotel_id) ON DELETE CASCADE,
    service_name VARCHAR(255) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    category VARCHAR(50),
    is_available BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS room_service_orders (
    order_id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(booking_id) ON DELETE CASCADE,
    total_amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_room_services_hotel ON room_services(hotel_id);
CREATE INDEX IF NOT EXISTS idx_room_service_orders_booking ON room_service_orders(booking_id);
