-- V7__Seed_large_dataset.sql
-- Description: Programmatically seed 100+ realistic records into all database tables for testing

-- 1. Seed 100 Users (Guest roles)
-- Mật khẩu mặc định: "Password123"
-- Hash: $2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS
DECLARE @i INT = 1;
WHILE @i <= 100
BEGIN
    INSERT INTO users (email, password_hash, full_name, role, status, phone_number, identification_number, created_at, updated_at)
    VALUES (
        CONCAT('guest', @i, '@luxury.com'),
        '$2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS',
        CONCAT('Guest User ', @i),
        'ROLE_USER',
        'ACTIVE',
        CONCAT('0912', RIGHT('000000' + CAST(@i AS VARCHAR(6)), 6)),
        CONCAT('001206', RIGHT('000000' + CAST(@i AS VARCHAR(6)), 6)),
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    SET @i = @i + 1;
END;

-- 2. Seed 96 Hotels (making 100 hotels total with the existing 4)
-- Spreads hotels across major travel hubs in Vietnam
DECLARE @h INT = 1;
DECLARE @city NVARCHAR(50);
DECLARE @rating DECIMAL(3,2);
WHILE @h <= 96
BEGIN
    SET @city = CASE @h % 10
        WHEN 0 THEN N'Hanoi'
        WHEN 1 THEN N'Da Nang'
        WHEN 2 THEN N'Nha Trang'
        WHEN 3 THEN N'Phu Quoc'
        WHEN 4 THEN N'Ha Long'
        WHEN 5 THEN N'Ho Chi Minh City'
        WHEN 6 THEN N'Da Lat'
        WHEN 7 THEN N'Sa Pa'
        WHEN 8 THEN N'Hoi An'
        WHEN 9 THEN N'Vung Tau'
    END;
    SET @rating = 3.5 + (CAST(ABS(CHECKSUM(NEWID())) % 16 AS DECIMAL(5,2)) / 10.0); -- 3.50 to 5.00
    
    INSERT INTO hotels (name, location, description, rating, is_active, created_at, updated_at)
    VALUES (
        CONCAT(
            CASE @h % 4
                WHEN 0 THEN N'Grand Plaza '
                WHEN 1 THEN N'Ocean Breeze '
                WHEN 2 THEN N'Royal Palace '
                WHEN 3 THEN N'Golden Silk '
            END,
            CASE @h % 3
                WHEN 0 THEN N'Resort & Spa'
                WHEN 1 THEN N'Boutique Hotel'
                WHEN 2 THEN N'Luxury Hotel'
            END,
            ' ', @h
        ),
        CONCAT(N'Street ', @h, N', ', @city, N', Vietnam'),
        CONCAT(N'Experience a premium and relaxing stay at our hotel in ', @city, N'. Offering state-of-the-art facilities and top-tier service.'),
        @rating,
        1,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    SET @h = @h + 1;
END;

-- 3. Seed Hotel Images for the new 96 hotels (making 101 images total)
INSERT INTO hotel_images (hotel_id, image_url, image_format, created_at, updated_at)
SELECT 
    hotel_id,
    CASE hotel_id % 5
        WHEN 0 THEN 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80'
        WHEN 1 THEN 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80'
        WHEN 2 THEN 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80'
        WHEN 3 THEN 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80'
        WHEN 4 THEN 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=800&q=80'
    END,
    'jpg',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM hotels
WHERE hotel_id > 4;

-- 4. Seed Rooms for all 96 new hotels (adding 2.5 rooms per hotel on average, making 252 rooms total)
DECLARE @hotel_id BIGINT;
DECLARE hotel_cursor CURSOR FOR 
    SELECT hotel_id FROM hotels WHERE hotel_id > 4;

OPEN hotel_cursor;
FETCH NEXT FROM hotel_cursor INTO @hotel_id;

WHILE @@FETCH_STATUS = 0
BEGIN
    -- Standard Room
    INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at)
    VALUES (
        @hotel_id,
        N'Standard Room',
        80.00 + (CAST(ABS(CHECKSUM(NEWID())) % 50 AS DECIMAL(18,2))),
        CONCAT('10', CAST((@hotel_id * 7) % 10 AS VARCHAR)),
        'AVAILABLE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

    -- Deluxe Suite
    INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at)
    VALUES (
        @hotel_id,
        N'Deluxe Suite',
        150.00 + (CAST(ABS(CHECKSUM(NEWID())) % 100 AS DECIMAL(18,2))),
        CONCAT('20', CAST((@hotel_id * 9) % 10 AS VARCHAR)),
        'AVAILABLE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

    -- Presidential Penthouse (for even hotels)
    IF @hotel_id % 2 = 0
    BEGIN
        INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at)
        VALUES (
            @hotel_id,
            N'Presidential Penthouse',
            400.00 + (CAST(ABS(CHECKSUM(NEWID())) % 300 AS DECIMAL(18,2))),
            CONCAT('30', CAST((@hotel_id * 3) % 10 AS VARCHAR)),
            'AVAILABLE',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END;

    FETCH NEXT FROM hotel_cursor INTO @hotel_id;
END;

CLOSE hotel_cursor;
DEALLOCATE hotel_cursor;

-- 5. Seed 120 Bookings (100 COMPLETED so we can write 100 reviews, 20 CONFIRMED for active stays)
DECLARE @b INT = 1;
DECLARE @usr_id BIGINT;
DECLARE @htl_id BIGINT;
DECLARE @chk_in DATETIME2;
DECLARE @chk_out DATETIME2;
DECLARE @amt DECIMAL(18,2);
DECLARE @bk_status VARCHAR(50);

WHILE @b <= 120
BEGIN
    -- Get corresponding guest user ID using OFFSET/FETCH NEXT
    SET @usr_id = (SELECT user_id FROM users WHERE email LIKE 'guest%' ORDER BY user_id OFFSET (@b % 100) ROWS FETCH NEXT 1 ROWS ONLY);
    -- Get corresponding hotel ID using OFFSET/FETCH NEXT
    SET @htl_id = (SELECT hotel_id FROM hotels ORDER BY hotel_id OFFSET (@b % 95) ROWS FETCH NEXT 1 ROWS ONLY);
    
    -- Spread dates out: first 100 are past stays, last 20 are future stays
    IF @b <= 100
    BEGIN
        SET @chk_in = DATEADD(day, -(@b * 3 + 2), CURRENT_TIMESTAMP);
        SET @bk_status = 'COMPLETED';
    END
    ELSE
    BEGIN
        SET @chk_in = DATEADD(day, (@b % 10 + 2), CURRENT_TIMESTAMP);
        SET @bk_status = 'CONFIRMED';
    END;
    
    SET @chk_out = DATEADD(day, 2 + (@b % 3), @chk_in);
    SET @amt = 250.00 + (@b * 12.50);

    INSERT INTO bookings (booking_code, user_id, hotel_id, check_in_date, check_out_date, total_amount, status, created_at, updated_at)
    VALUES (
        CONCAT('BK-', 100000 + @b),
        @usr_id,
        @htl_id,
        @chk_in,
        @chk_out,
        @amt,
        @bk_status,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

    SET @b = @b + 1;
END;

-- 6. Seed 120 Booking Room Links
-- Link each booking to the first room of the selected hotel
INSERT INTO booking_rooms (booking_id, room_id, quantity, price_at_booking, created_at, updated_at)
SELECT 
    b.booking_id,
    (SELECT TOP 1 r.room_id FROM rooms r WHERE r.hotel_id = b.hotel_id),
    1,
    (SELECT TOP 1 r.price FROM rooms r WHERE r.hotel_id = b.hotel_id),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM bookings b;

-- 7. Seed 120 Payments (Matching the 120 bookings)
INSERT INTO payments (booking_id, payment_method, amount, status, created_at, updated_at)
SELECT 
    booking_id,
    CASE booking_id % 3
        WHEN 0 THEN 'CREDIT_CARD'
        WHEN 1 THEN 'PAYPAL'
        WHEN 2 THEN 'BANK_TRANSFER'
    END,
    total_amount,
    CASE status
        WHEN 'COMPLETED' THEN 'PAID'
        WHEN 'CONFIRMED' THEN 'PAID'
        ELSE 'PENDING'
    END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM bookings;

-- 8. Seed 100 Reviews (Matching the 100 COMPLETED bookings)
INSERT INTO reviews (user_id, hotel_id, booking_id, rating, comment, created_at, updated_at)
SELECT 
    user_id,
    hotel_id,
    booking_id,
    3 + (booking_id % 3), -- rating score: 3, 4, or 5
    CASE booking_id % 5
        WHEN 0 THEN N'Absolutely beautiful resort. Highly recommend the suite layouts and pools!'
        WHEN 1 THEN N'Decent room structure and friendly desk staff. Very peaceful stay.'
        WHEN 2 THEN N'Amazing scenic environments. The room view exceeded my expectations.'
        WHEN 3 THEN N'Good services and fast wifi. Overall standard stays were perfect.'
        WHEN 4 THEN N'Wonderful and luxurious atmosphere. Will book again in my next vacation!'
    END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM bookings
WHERE status = 'COMPLETED';

-- 9. Seed 100 User Favorites
DECLARE @f INT = 1;
DECLARE @fav_usr BIGINT;
DECLARE @fav_htl BIGINT;
WHILE @f <= 100
BEGIN
    SET @fav_usr = (SELECT user_id FROM users WHERE email LIKE 'guest%' ORDER BY user_id OFFSET (@f % 100) ROWS FETCH NEXT 1 ROWS ONLY);
    SET @fav_htl = (SELECT hotel_id FROM hotels ORDER BY hotel_id OFFSET ((@f * 7) % 95) ROWS FETCH NEXT 1 ROWS ONLY);
    
    -- Ensure no duplicate favorites during generation
    IF NOT EXISTS (SELECT 1 FROM favorites WHERE user_id = @fav_usr AND hotel_id = @fav_htl)
    BEGIN
        INSERT INTO favorites (user_id, hotel_id, created_at, updated_at)
        VALUES (@fav_usr, @fav_htl, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
    END;
    
    SET @f = @f + 1;
END;

-- 10. Seed 100 Support Tickets
DECLARE @t INT = 1;
DECLARE @tkt_usr BIGINT;
DECLARE @staff_usr BIGINT;
SET @staff_usr = (SELECT TOP 1 user_id FROM users WHERE email = 'staff@hotel.com');

WHILE @t <= 100
BEGIN
    SET @tkt_usr = (SELECT user_id FROM users WHERE email LIKE 'guest%' ORDER BY user_id OFFSET (@t % 100) ROWS FETCH NEXT 1 ROWS ONLY);
    
    INSERT INTO support_tickets (user_id, staff_id, issue_description, status, created_at, updated_at)
    VALUES (
        @tkt_usr,
        CASE @t % 2 WHEN 0 THEN @staff_usr ELSE NULL END, -- 50% assigned to staff
        CONCAT(
            CASE @t % 5
                WHEN 0 THEN N'My checkout date needs to be modified'
                WHEN 1 THEN N'Do you support airport shuttle pickups?'
                WHEN 2 THEN N'Transaction processed twice on my credit card'
                WHEN 3 THEN N'Are gym facilities accessible all day?'
                WHEN 4 THEN N'I would like to request extra towels for room'
            END,
            ' - Reference Ticket #', @t
        ),
        CASE @t % 3
            WHEN 0 THEN 'OPEN'
            WHEN 1 THEN 'IN_PROGRESS'
            WHEN 2 THEN 'RESOLVED'
        END,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    SET @t = @t + 1;
END;
