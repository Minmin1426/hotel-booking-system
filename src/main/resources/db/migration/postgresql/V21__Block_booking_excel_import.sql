-- V21__Block_booking_excel_import.sql (PostgreSQL)
-- Spec 008: Block Booking Excel Import

CREATE TABLE IF NOT EXISTS block_booking_requests (
    block_booking_id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    total_guests INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    rejection_reason TEXT,
    approved_by BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_block_requests_users FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_block_requests_approver FOREIGN KEY (approved_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_block_requests_requester ON block_booking_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_block_requests_status ON block_booking_requests(status);
CREATE INDEX IF NOT EXISTS idx_block_requests_created ON block_booking_requests(created_at DESC);

CREATE TABLE IF NOT EXISTS block_booking_rows (
    row_id BIGSERIAL PRIMARY KEY,
    block_booking_id BIGINT NOT NULL,
    guest_name VARCHAR(100),
    email VARCHAR(255),
    phone_number VARCHAR(30),
    hotel_id BIGINT,
    check_in_date DATE,
    check_out_date DATE,
    room_type VARCHAR(50),
    quantity INT DEFAULT 1,
    booking_id BIGINT,
    row_status VARCHAR(20) NOT NULL DEFAULT 'VALID',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_block_rows_requests FOREIGN KEY (block_booking_id) REFERENCES block_booking_requests(block_booking_id) ON DELETE CASCADE,
    CONSTRAINT FK_block_rows_hotels FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id) ON DELETE SET NULL,
    CONSTRAINT FK_block_rows_bookings FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_block_rows_request ON block_booking_rows(block_booking_id);
CREATE INDEX IF NOT EXISTS idx_block_rows_status ON block_booking_rows(row_status);
