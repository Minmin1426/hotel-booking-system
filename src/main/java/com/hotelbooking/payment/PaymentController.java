package com.hotelbooking.payment;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR', 'RECEPTIONIST')")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO requestDTO) {
        PaymentResponseDTO response = paymentService.createPaymentRequest(requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR', 'RECEPTIONIST')")
    public ResponseEntity<String> verifyPayment(@RequestParam String paymentIntentId) {
        try {
            String status = paymentService.verifyPayment(paymentIntentId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to verify payment");
        }
    }

    @PostMapping("/{bookingId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> processRefund(@PathVariable Long bookingId) {
        paymentService.processRefund(bookingId);
        return ResponseEntity.ok("Refund processed successfully.");
    }
    
    @PostMapping("/{paymentId}/confirm-cash")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'DIRECTOR')")
    public ResponseEntity<String> confirmCashPayment(@PathVariable Long paymentId) {
        paymentService.confirmCashPayment(paymentId);
        return ResponseEntity.ok("Cash payment confirmed successfully.");
    }

    @PostMapping("/{paymentId}/confirm-bank")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'DIRECTOR')")
    public ResponseEntity<String> confirmBankTransfer(@PathVariable Long paymentId) {
        paymentService.confirmBankTransfer(paymentId);
        return ResponseEntity.ok("Bank transfer confirmed successfully.");
    }

    @PostMapping("/{bookingId}/refund-meals")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'STAFF', 'ADMIN')")
    public ResponseEntity<String> refundUnusedMealTickets(
            @PathVariable Long bookingId,
            @RequestBody Map<String, BigDecimal> body) {
        BigDecimal unusedAmount = body.get("unusedAmount");
        if (unusedAmount == null) {
            return ResponseEntity.badRequest().body("unusedAmount is required");
        }
        paymentService.refundUnusedMealTickets(bookingId, unusedAmount);
        return ResponseEntity.ok("Meal tickets refunded successfully.");
    }

    @GetMapping("/{paymentId}/invoice")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RECEPTIONIST', 'STAFF', 'ADMIN', 'DIRECTOR')")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long paymentId) {
        byte[] pdfBytes = paymentService.generateInvoicePdf(paymentId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_" + paymentId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/payout/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<Payout> calculateMonthlyPayout(
            @RequestParam Long hotelId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Payout payout = paymentService.calculateMonthlyPayout(hotelId, start, end);
        return ResponseEntity.ok(payout);
    }

    @PostMapping("/payout/{payoutId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<String> approvePayout(@PathVariable Long payoutId) {
        paymentService.approvePayout(payoutId);
        return ResponseEntity.ok("Payout approved and paid successfully.");
    }

    @GetMapping("/payout/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<Payout>> getPayoutsByHotel(@PathVariable Long hotelId) {
        List<Payout> payouts = paymentService.getPayoutsByHotel(hotelId);
        return ResponseEntity.ok(payouts);
    }
}
