package com.hotelbooking.controller;

import com.hotelbooking.dto.PaymentRequestDTO;
import com.hotelbooking.dto.PaymentResponseDTO;
import com.hotelbooking.dto.WebhookCallbackDTO;
import com.hotelbooking.service.PaymentService;
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO requestDTO) {
        PaymentResponseDTO response = paymentService.createPaymentRequest(requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-Signature") String signature,
            @RequestBody String rawPayload) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            WebhookCallbackDTO callbackDTO = mapper.readValue(rawPayload, WebhookCallbackDTO.class);
            
            paymentService.processWebhook(callbackDTO, signature, rawPayload);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing webhook");
        }
    }

    @PostMapping("/{bookingId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> processRefund(@PathVariable Long bookingId) {
        paymentService.processRefund(bookingId);
        return ResponseEntity.ok("Refund processed successfully.");
    }
}
