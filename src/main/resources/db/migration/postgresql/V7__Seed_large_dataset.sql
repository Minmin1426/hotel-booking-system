-- V7__Seed_large_dataset.sql
-- Description: Programmatically seed 100+ realistic records into all database tables for testing (PostgreSQL)

DO $$
DECLARE
    i INT;
    h INT;
    city VARCHAR(50);
    rating DECIMAL(3,2);
    hotel_record RECORD;
    user_id_val BIGINT;
    hotel_id_val BIGINT;
    chk_in TIMESTAMP;
    chk_out TIMESTAMP;
    amt DECIMAL(18,2);
    bk_status VARCHAR(50);
    staff_id_val BIGINT;
    user_ids BIGINT[];
    hotel_ids BIGINT[];
    user_count INT;
    hotel_count INT;
    f INT;
    t INT;
BEGIN
    -- 1. Seed 100 Users (Guest roles)
    -- Mật khẩu mặc định: "Password123"
    -- Hash: $2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS
    FOR i IN 1..100 LOOP
        INSERT INTO users (email, password_hash, full_name, role, status, phone_number, identification_number, created_at, updated_at)
        VALUES (
            'guest' || i || '@luxury.com',
            '$2a$12$L3sJCBuc/.OcGDhJtu1XYOJVKKMxlM0TKXtS8c4OzEZSxEzbSu/xS',
            'Guest User ' || i,
            'ROLE_USER',
            'ACTIVE',
            '0912' || lpad(i::text, 6, '0'),
            '001206' || lpad(i::text, 6, '0'),
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
        ON CONFLICT (email) DO NOTHING;
    END LOOP;

    -- 2. Seed 96 Hotels (making 100 hotels total with the existing 4)
    FOR h IN 1..96 LOOP
        city := CASE h % 10
            WHEN 0 THEN 'Hanoi'
            WHEN 1 THEN 'Da Nang'
            WHEN 2 THEN 'Nha Trang'
            WHEN 3 THEN 'Phu Quoc'
            WHEN 4 THEN 'Ha Long'
            WHEN 5 THEN 'Ho Chi Minh City'
            WHEN 6 THEN 'Da Lat'
            WHEN 7 THEN 'Sa Pa'
            WHEN 8 THEN 'Hoi An'
            WHEN 9 THEN 'Vung Tau'
        END;
        rating := 3.5 + ((h % 16)::numeric / 10.0);
        
        INSERT INTO hotels (name, location, description, rating, is_active, created_at, updated_at)
        VALUES (
            CASE h % 4
                WHEN 0 THEN 'Grand Plaza '
                WHEN 1 THEN 'Ocean Breeze '
                WHEN 2 THEN 'Royal Palace '
                WHEN 3 THEN 'Golden Silk '
            END || 
            CASE h % 3
                WHEN 0 THEN 'Resort & Spa'
                WHEN 1 THEN 'Boutique Hotel'
                WHEN 2 THEN 'Luxury Hotel'
            END || ' ' || h,
            'Street ' || h || ', ' || city || ', Vietnam',
            'Experience a premium and relaxing stay at our hotel in ' || city || '. Offering state-of-the-art facilities and top-tier service.',
            rating,
            TRUE,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END LOOP;

    -- 3. Seed Hotel Images for the new 96 hotels
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

    -- 4. Seed Rooms for all 96 new hotels
    FOR hotel_record IN SELECT hotel_id FROM hotels WHERE hotel_id > 4 LOOP
        INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at)
        VALUES (
            hotel_record.hotel_id,
            'Standard Room',
            80.00 + ((hotel_record.hotel_id % 50)::numeric),
            '10' || ((hotel_record.hotel_id * 7) % 10)::text,
            'AVAILABLE',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );

        INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at)
        VALUES (
            hotel_record.hotel_id,
            'Deluxe Suite',
            150.00 + ((hotel_record.hotel_id % 100)::numeric),
            '20' || ((hotel_record.hotel_id * 9) % 10)::text,
            'AVAILABLE',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );

        IF hotel_record.hotel_id % 2 = 0 THEN
            INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at)
            VALUES (
                hotel_record.hotel_id,
                'Presidential Penthouse',
                400.00 + ((hotel_record.hotel_id % 300)::numeric),
                '30' || ((hotel_record.hotel_id * 3) % 10)::text,
                'AVAILABLE',
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END IF;
    END LOOP;

    -- Retrieve arrays for user_ids and hotel_ids
    SELECT ARRAY(SELECT user_id FROM users WHERE email LIKE 'guest%' ORDER BY user_id) INTO user_ids;
    SELECT ARRAY(SELECT hotel_id FROM hotels ORDER BY hotel_id) INTO hotel_ids;
    user_count := array_length(user_ids, 1);
    hotel_count := array_length(hotel_ids, 1);

    -- 5. Seed 120 Bookings
    FOR b IN 1..120 LOOP
        user_id_val := user_ids[(b % user_count) + 1];
        hotel_id_val := hotel_ids[(b % (hotel_count - 5)) + 1];
        
        IF b <= 100 THEN
            chk_in := CURRENT_TIMESTAMP - (b * 3 + 2) * INTERVAL '1 day';
            bk_status := 'COMPLETED';
        ELSE
            chk_in := CURRENT_TIMESTAMP + (b % 10 + 2) * INTERVAL '1 day';
            bk_status := 'CONFIRMED';
        END IF;
        
        chk_out := chk_in + (2 + (b % 3)) * INTERVAL '1 day';
        amt := 250.00 + (b * 12.50);

        INSERT INTO bookings (booking_code, user_id, hotel_id, check_in_date, check_out_date, total_amount, status, created_at, updated_at)
        VALUES (
            'BK-' || (100000 + b)::text,
            user_id_val,
            hotel_id_val,
            chk_in,
            chk_out,
            amt,
            bk_status,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
        ON CONFLICT (booking_code) DO NOTHING;
    END LOOP;

    -- 6. Seed 120 Booking Room Links
    INSERT INTO booking_rooms (booking_id, room_id, quantity, price_at_booking, created_at, updated_at)
    SELECT 
        b.booking_id,
        (SELECT r.room_id FROM rooms r WHERE r.hotel_id = b.hotel_id LIMIT 1),
        1,
        (SELECT r.price FROM rooms r WHERE r.hotel_id = b.hotel_id LIMIT 1),
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM bookings b
    ON CONFLICT DO NOTHING;

    -- 7. Seed 120 Payments
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

    -- 8. Seed 100 Reviews
    INSERT INTO reviews (user_id, hotel_id, booking_id, rating, comment, created_at, updated_at)
    SELECT 
        user_id,
        hotel_id,
        booking_id,
        3 + (booking_id % 3),
        CASE booking_id % 5
            WHEN 0 THEN 'Absolutely beautiful resort. Highly recommend the suite layouts and pools!'
            WHEN 1 THEN 'Decent room structure and friendly desk staff. Very peaceful stay.'
            WHEN 2 THEN 'Amazing scenic environments. The room view exceeded my expectations.'
            WHEN 3 THEN 'Good services and fast wifi. Overall standard stays were perfect.'
            WHEN 4 THEN 'Wonderful and luxurious atmosphere. Will book again in my next vacation!'
        END,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM bookings
    WHERE status = 'COMPLETED'
    ON CONFLICT (booking_id) DO NOTHING;

    -- 9. Seed 100 User Favorites
    FOR f IN 1..100 LOOP
        user_id_val := user_ids[(f % user_count) + 1];
        hotel_id_val := hotel_ids[((f * 7) % hotel_count) + 1];
        
        BEGIN
            INSERT INTO favorites (user_id, hotel_id, created_at, updated_at)
            VALUES (user_id_val, hotel_id_val, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        EXCEPTION WHEN OTHERS THEN
            -- ignore duplicates
        END;
    END LOOP;

    -- 10. Seed 100 Support Tickets
    SELECT user_id FROM users WHERE email = 'staff@hotel.com' LIMIT 1 INTO staff_id_val;

    FOR t IN 1..100 LOOP
        user_id_val := user_ids[(t % user_count) + 1];
        
        INSERT INTO support_tickets (user_id, staff_id, issue_description, status, created_at, updated_at)
        VALUES (
            user_id_val,
            CASE t % 2 WHEN 0 THEN staff_id_val ELSE NULL END,
            CASE t % 5
                WHEN 0 THEN 'My checkout date needs to be modified'
                WHEN 1 THEN 'Do you support airport shuttle pickups?'
                WHEN 2 THEN 'Transaction processed twice on my credit card'
                WHEN 3 THEN 'Are gym facilities accessible all day?'
                WHEN 4 THEN 'I would like to request extra towels for room'
            END || ' - Reference Ticket #' || t,
            CASE t % 3
                WHEN 0 THEN 'OPEN'
                WHEN 1 THEN 'IN_PROGRESS'
                WHEN 2 THEN 'RESOLVED'
            END,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;
