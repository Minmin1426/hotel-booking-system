package com.hotelbooking.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class VnpayCallbackController {

    private final PaymentService paymentService;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/vnpay-callback")
    public ResponseEntity<Void> handleVnpayCallback(
            @RequestParam Map<String, String> params,
            jakarta.servlet.http.HttpServletRequest request) {
        log.info("Processing VNPAY Callback: {}", params);
        String targetFrontend = resolveFrontendUrl(request);
        try {
            paymentService.processVnpayCallback(params);
            
            HttpHeaders headers = new HttpHeaders();
            String txnRef = params.get("vnp_TxnRef");
            String redirectUrl = targetFrontend + "/payment/success?payment_intent=" + (txnRef != null ? txnRef : "");
            log.info("VNPAY callback succeeded, redirecting to: {}", redirectUrl);
            headers.setLocation(URI.create(redirectUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (Exception e) {
            log.error("Error processing VNPAY Callback", e);
            HttpHeaders headers = new HttpHeaders();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "VNPAY callback processing failed";
            String redirectUrl = targetFrontend + "/payment/cancel?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
            log.info("VNPAY callback failed, redirecting to: {}", redirectUrl);
            headers.setLocation(URI.create(redirectUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    private String resolveFrontendUrl(jakarta.servlet.http.HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                URI uri = URI.create(referer);
                if (uri.getScheme() != null && uri.getAuthority() != null) {
                    return uri.getScheme() + "://" + uri.getAuthority();
                }
            } catch (Exception ignored) {}
        }
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            return origin;
        }
        return frontendUrl;
    }

    @RequestMapping(value = "/vnpay-ipn", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> handleVnpayIpn(@RequestParam Map<String, String> params) {
        log.info("Received VNPAY IPN: {}", params);
        Map<String, String> response = new java.util.HashMap<>();
        try {
            paymentService.processVnpayCallback(params);
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.error("VNPAY IPN signature verification failed", e);
            response.put("RspCode", "97");
            response.put("Message", "Invalid Signature");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("VNPAY IPN internal error", e);
            response.put("RspCode", "99");
            response.put("Message", "Input Required / Error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
