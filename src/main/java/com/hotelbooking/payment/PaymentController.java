package com.hotelbooking.payment;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
