-- V21__Block_booking_excel_import.sql (SQL Server)
-- Spec 008: Block Booking Excel Import

CREATE TABLE block_booking_requests (
    block_booking_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    file_name NVARCHAR(255),
    total_guests INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    status NVARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    rejection_reason NVARCHAR(MAX),
    approved_by BIGINT,
    approved_at DATETIME2,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_block_requests_users FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_block_requests_approver FOREIGN KEY (approved_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_block_requests_requester ON block_booking_requests(requester_id);
CREATE INDEX idx_block_requests_status ON block_booking_requests(status);
CREATE INDEX idx_block_requests_created ON block_booking_requests(created_at DESC);

CREATE TABLE block_booking_rows (
    row_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    block_booking_id BIGINT NOT NULL,
    guest_name NVARCHAR(100),
    email NVARCHAR(255),
    phone_number NVARCHAR(30),
    hotel_id BIGINT,
    check_in_date DATE,
    check_out_date DATE,
    room_type NVARCHAR(50),
    quantity INT DEFAULT 1,
    booking_id BIGINT,
    row_status NVARCHAR(20) NOT NULL DEFAULT 'VALID',
    error_message NVARCHAR(MAX),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_block_rows_requests FOREIGN KEY (block_booking_id) REFERENCES block_booking_requests(block_booking_id) ON DELETE CASCADE,
    CONSTRAINT FK_block_rows_hotels FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id) ON DELETE SET NULL,
    CONSTRAINT FK_block_rows_bookings FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE SET NULL
);

CREATE INDEX idx_block_rows_request ON block_booking_rows(block_booking_id);
CREATE INDEX idx_block_rows_status ON block_booking_rows(row_status);
