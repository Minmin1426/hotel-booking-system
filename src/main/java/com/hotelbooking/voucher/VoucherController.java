package com.hotelbooking.voucher;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.voucher.dto.ApplyVoucherRequestDTO;
import com.hotelbooking.voucher.dto.VoucherResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VoucherResponse>> getAllActiveVouchers() {
        List<VoucherResponse> vouchers = voucherService.getAllActiveVouchers();
        return ResponseEntity.ok(vouchers);
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR')")
    public ResponseEntity<String> applyVoucher(@Valid @RequestBody ApplyVoucherRequestDTO requestDTO) {
        Booking booking = voucherService.applyVoucher(requestDTO.getBookingId(), requestDTO.getVoucherCode());
        return ResponseEntity.ok("Voucher applied successfully. New total: " + booking.getFinalPrice());
    }
}
