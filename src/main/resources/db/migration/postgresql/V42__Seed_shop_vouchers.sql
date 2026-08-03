-- V42__Seed_shop_vouchers.sql
-- Description: Add name/description/is_active columns to vouchers (if missing) and seed shop vouchers

-- Ensure columns exist (entity has them but earlier V9 may not have created them)
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- Seed loyalty-points-based shop vouchers
INSERT INTO vouchers (code, name, description, discount_type, discount_value, min_booking_value,
                      start_date, end_date, max_usage, current_usage, is_active, for_account_type,
                      points_cost, created_at, updated_at)
VALUES
    ('SHOP500_5',    '5% Off Voucher',      'Get 5% off your next booking',      'PERCENTAGE',   5.00,   0.00,   '2026-01-01 00:00:00', '2030-12-31 23:59:59', 1000, 0, TRUE, 'ALL', 500,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SHOP1000_10',  '10% Off Voucher',     'Get 10% off your next booking',     'PERCENTAGE',  10.00,  50.00,   '2026-01-01 00:00:00', '2030-12-31 23:59:59', 1000, 0, TRUE, 'ALL', 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SHOP1500_15',  '15% Off Voucher',     'Get 15% off your next booking',     'PERCENTAGE',  15.00,  100.00,  '2026-01-01 00:00:00', '2030-12-31 23:59:59', 1000, 0, TRUE, 'ALL', 1500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SHOP2000_20',  '20% Off Voucher',     'Get 20% off your next booking',     'PERCENTAGE',  20.00,  200.00,  '2026-01-01 00:00:00', '2030-12-31 23:59:59', 1000, 0, TRUE, 'ALL', 2000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SHOP3000_25',  '25% Off Voucher',     'Get 25% off your next booking',     'PERCENTAGE',  25.00,  300.00,  '2026-01-01 00:00:00', '2030-12-31 23:59:59', 1000, 0, TRUE, 'ALL', 3000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SHOP500_25K',  '25 USD Off Voucher',  'Get 25 USD off your next booking',  'FIXED_AMOUNT', 25.00, 100.00,   '2026-01-01 00:00:00', '2030-12-31 23:59:59', 500,  0, TRUE, 'ALL', 500,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SHOP1500_50',  '50 USD Off Voucher',  'Get 50 USD off your next booking',  'FIXED_AMOUNT', 50.00, 200.00,   '2026-01-01 00:00:00', '2030-12-31 23:59:59', 500,  0, TRUE, 'ALL', 1500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SHOP3000_100', '100 USD Off Voucher', 'Get 100 USD off your next booking', 'FIXED_AMOUNT', 100.00, 500.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 500,  0, TRUE, 'ALL', 3000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;
