-- V13__Seed_vouchers.sql
-- Description: Seed active test vouchers

INSERT INTO vouchers (code, discount_type, discount_value, min_booking_value, start_date, end_date, max_usage, current_usage, created_at, updated_at)
VALUES 
('WELCOME10', 'PERCENTAGE', 10.00, 0.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 1000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HOTEL50', 'FIXED_AMOUNT', 50.00, 100.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SUMMER20', 'PERCENTAGE', 20.00, 0.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 1000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
