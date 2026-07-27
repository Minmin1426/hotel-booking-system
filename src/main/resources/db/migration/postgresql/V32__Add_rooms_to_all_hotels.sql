-- V32__Add_rooms_to_all_hotels.sql
-- Description: Programmatically insert 15 additional standard rooms to every hotel to support group booking demo on any hotel

DO $$
DECLARE
    h RECORD;
    i INT;
BEGIN
    FOR h IN SELECT hotel_id FROM hotels LOOP
        FOR i IN 3..17 LOOP
            INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at)
            VALUES (
                h.hotel_id,
                'Standard Room',
                100.00,
                'R-' || h.hotel_id || '-' || i,
                'AVAILABLE',
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;
