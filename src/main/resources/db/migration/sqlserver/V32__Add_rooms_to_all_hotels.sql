-- V32__Add_rooms_to_all_hotels.sql
-- Description: Programmatically insert 15 additional standard rooms to every hotel to support group booking demo on any hotel

DECLARE @hotel_id BIGINT;
DECLARE @i INT;
DECLARE hotel_cursor CURSOR FOR SELECT hotel_id FROM hotels;

OPEN hotel_cursor;
FETCH NEXT FROM hotel_cursor INTO @hotel_id;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @i = 3;
    WHILE @i <= 17
    BEGIN
        INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at)
        VALUES (
            @hotel_id, 
            'Standard Room', 
            100.00, 
            'R-' + CAST(@hotel_id AS VARCHAR) + '-' + CAST(@i AS VARCHAR), 
            'AVAILABLE', 
            CURRENT_TIMESTAMP, 
            CURRENT_TIMESTAMP
        );
        SET @i = @i + 1;
    END
    FETCH NEXT FROM hotel_cursor INTO @hotel_id;
END;

CLOSE hotel_cursor;
DEALLOCATE hotel_cursor;
