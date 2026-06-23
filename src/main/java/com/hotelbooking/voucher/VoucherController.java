package com.hotelbooking.voucher;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.voucher.dto.ApplyVoucherRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> applyVoucher(@Valid @RequestBody ApplyVoucherRequestDTO requestDTO) {
        Booking booking = voucherService.applyVoucher(requestDTO.getBookingId(), requestDTO.getVoucherCode());
        return ResponseEntity.ok("Voucher applied successfully. New total: " + booking.getFinalPrice());
    }
}
