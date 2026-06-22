package com.hotelbooking.service;

import com.hotelbooking.model.Booking;

public interface VoucherService {
    Booking applyVoucher(Long bookingId, String voucherCode);
}
