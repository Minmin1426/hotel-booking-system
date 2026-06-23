-- V14__Seed_more_vouchers.sql
-- Description: Seed more active test vouchers

INSERT INTO vouchers (code, discount_type, discount_value, min_booking_value, start_date, end_date, max_usage, current_usage, created_at, updated_at)
VALUES 
('LUXURY30', 'PERCENTAGE', 30.00, 200.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 1000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SPRING15', 'PERCENTAGE', 15.00, 0.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 1000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DISCOUNT100', 'FIXED_AMOUNT', 100.00, 300.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
