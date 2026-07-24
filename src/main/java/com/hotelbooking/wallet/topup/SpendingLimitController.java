package com.hotelbooking.wallet.topup;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.wallet.topup.dto.SpendingLimitRequest;
import com.hotelbooking.wallet.topup.dto.SpendingLimitResponse;
import com.hotelbooking.wallet.topup.dto.SpendingStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class SpendingLimitController {

    private final SpendingLimitService spendingLimitService;
    private final JwtService jwtService;

    // PUT /api/v1/groups/{groupId}/members/{userId}/spending-limit
    @PutMapping("/groups/{groupId}/members/{userId}/spending-limit")
    public ResponseEntity<ApiResponse<SpendingLimitResponse>> setMemberSpendingLimit(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody SpendingLimitRequest request) {
        Long setterId = extractUserId(authorizationHeader);
        SpendingLimitResponse response = spendingLimitService.setMemberLimit(setterId, groupId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Spending limit updated", response));
    }

    // GET /api/v1/users/me/spending-status
    @GetMapping("/users/me/spending-status")
    public ResponseEntity<ApiResponse<SpendingStatusResponse>> getSpendingStatus(
            @RequestParam Long groupId,
            @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = extractUserId(authorizationHeader);
        SpendingStatusResponse status = spendingLimitService.getSpendingStatus(userId, groupId);
        return ResponseEntity.ok(ApiResponse.success("Spending status retrieved", status));
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
