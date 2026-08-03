-- V42__Seed_test_vouchers.sql (SQL Server)

IF NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'TESTWELCOME10')
BEGIN
    INSERT INTO vouchers (
        code, name, description, discount_type, discount_value, min_booking_value, 
        max_discount, max_usage, current_usage, start_date, end_date, is_active, 
        voucher_type, combo_meal_benefit, for_account_type, points_cost
    ) VALUES (
        'TESTWELCOME10', 'Welcome 10% Off', 'Get 10% off your first hotel booking', 'PERCENTAGE', 10.00, 50.00, 
        50.00, 100, 0, CURRENT_TIMESTAMP, DATEADD(day, 30, CURRENT_TIMESTAMP), 1, 
        'STANDARD', NULL, 'ALL', 0
    );
END;

IF NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'TESTSUMMER20')
BEGIN
    INSERT INTO vouchers (
        code, name, description, discount_type, discount_value, min_booking_value, 
        max_discount, max_usage, current_usage, start_date, end_date, is_active, 
        voucher_type, combo_meal_benefit, for_account_type, points_cost
    ) VALUES (
        'TESTSUMMER20', 'Summer Promo $20', '$20 discount for summer bookings over $100', 'FIXED_AMOUNT', 20.00, 100.00, 
        NULL, 50, 0, CURRENT_TIMESTAMP, DATEADD(day, 60, CURRENT_TIMESTAMP), 1, 
        'STANDARD', NULL, 'ALL', 0
    );
END;

IF NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'TESTCOMBOVIP')
BEGIN
    INSERT INTO vouchers (
        code, name, description, discount_type, discount_value, min_booking_value, 
        max_discount, max_usage, current_usage, start_date, end_date, is_active, 
        voucher_type, combo_meal_benefit, for_account_type, points_cost
    ) VALUES (
        'TESTCOMBOVIP', 'VIP Breakfast Combo', 'Special combo voucher with free breakfast included', 'PERCENTAGE', 15.00, 150.00, 
        100.00, 30, 0, CURRENT_TIMESTAMP, DATEADD(day, 90, CURRENT_TIMESTAMP), 1, 
        'COMBO_MEAL', 'Free Breakfast Buffet for 2', 'ALL', 0
    );
END;
