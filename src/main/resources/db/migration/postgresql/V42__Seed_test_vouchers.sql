-- V42__Seed_test_vouchers.sql (PostgreSQL)

INSERT INTO vouchers (
    code, name, description, discount_type, discount_value, min_booking_value, 
    max_discount, max_usage, current_usage, start_date, end_date, is_active, 
    voucher_type, combo_meal_benefit, for_account_type, points_cost
) VALUES 
('TESTWELCOME10', 'Welcome 10% Off', 'Get 10% off your first hotel booking', 'PERCENTAGE', 10.00, 50.00, 50.00, 100, 0, NOW(), NOW() + INTERVAL '30 days', true, 'STANDARD', NULL, 'ALL', 0),
('TESTSUMMER20', 'Summer Promo $20', '$20 discount for summer bookings over $100', 'FIXED_AMOUNT', 20.00, 100.00, NULL, 50, 0, NOW(), NOW() + INTERVAL '60 days', true, 'STANDARD', NULL, 'ALL', 0),
('TESTCOMBOVIP', 'VIP Breakfast Combo', 'Special combo voucher with free breakfast included', 'PERCENTAGE', 15.00, 150.00, 100.00, 30, 0, NOW(), NOW() + INTERVAL '90 days', true, 'COMBO_MEAL', 'Free Breakfast Buffet for 2', 'ALL', 0)
ON CONFLICT (code) DO NOTHING;
