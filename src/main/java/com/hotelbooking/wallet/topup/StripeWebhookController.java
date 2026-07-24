package com.hotelbooking.wallet.topup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final TopUpService topUpService;

    // POST /webhooks/stripe/topup
    @PostMapping("/topup")
    public ResponseEntity<String> handleTopUpWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        topUpService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok("OK");
    }
}
