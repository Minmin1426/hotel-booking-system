package com.hotelbooking.mealticket;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.mealticket.dto.ScanTicketRequest;
import com.hotelbooking.mealticket.dto.ScanTicketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/restaurant")
@RequiredArgsConstructor
@Slf4j
public class RestaurantScanController {

    private final MealTicketService mealTicketService;
    private final JwtService jwtService;

    // POST /api/v1/restaurant/scan-ticket
    @PostMapping("/scan-ticket")
    @PreAuthorize("hasAnyRole('STAFF', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<ApiResponse<ScanTicketResponse>> scanTicket(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ScanTicketRequest request) {
        Long staffUserId = extractUserId(authorizationHeader);
        ScanTicketResponse response = mealTicketService.scanAndConsume(request.getQrCode(), staffUserId);
        return ResponseEntity.ok(ApiResponse.success("Ticket scanned and consumed", response));
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
