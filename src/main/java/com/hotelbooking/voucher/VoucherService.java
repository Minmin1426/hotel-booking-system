package com.hotelbooking.voucher;
import com.hotelbooking.booking.Booking;

public interface VoucherService {
    Booking applyVoucher(Long bookingId, String voucherCode);
}
