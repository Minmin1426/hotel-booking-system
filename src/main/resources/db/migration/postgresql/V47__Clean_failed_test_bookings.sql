-- V47: Clean failed test bookings from local database
-- Target only failed bookings that are not part of the seeded dataset (seed bookings start with BK-100)

DELETE FROM room_locks 
WHERE booking_id IN (SELECT booking_id FROM bookings WHERE status = 'FAILED' AND booking_code NOT LIKE 'BK-1%');

DELETE FROM booking_rooms 
WHERE booking_id IN (SELECT booking_id FROM bookings WHERE status = 'FAILED' AND booking_code NOT LIKE 'BK-1%');

DELETE FROM payments 
WHERE booking_id IN (SELECT booking_id FROM bookings WHERE status = 'FAILED' AND booking_code NOT LIKE 'BK-1%');

DELETE FROM reviews 
WHERE booking_id IN (SELECT booking_id FROM bookings WHERE status = 'FAILED' AND booking_code NOT LIKE 'BK-1%');

DELETE FROM meal_tickets 
WHERE booking_id IN (SELECT booking_id FROM bookings WHERE status = 'FAILED' AND booking_code NOT LIKE 'BK-1%');

DELETE FROM refund_audit_logs 
WHERE booking_id IN (SELECT booking_id FROM bookings WHERE status = 'FAILED' AND booking_code NOT LIKE 'BK-1%');

DELETE FROM bookings 
WHERE status = 'FAILED' AND booking_code NOT LIKE 'BK-1%';
