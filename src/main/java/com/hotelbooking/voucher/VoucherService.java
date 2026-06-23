package com.hotelbooking.voucher;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.voucher.dto.VoucherResponse;
import java.util.List;

public interface VoucherService {
    Booking applyVoucher(Long bookingId, String voucherCode);
    List<VoucherResponse> getAllActiveVouchers();
}
