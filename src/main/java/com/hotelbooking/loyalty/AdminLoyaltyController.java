package com.hotelbooking.loyalty;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.loyalty.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminLoyaltyController {

    private final LoyaltyService loyaltyService;
    private final JwtService jwtService;

    // GET /api/v1/admin/tier-definitions
    @GetMapping("/tier-definitions")
    public ResponseEntity<ApiResponse<List<TierDefinitionResponse>>> getTierDefinitions(
            @RequestParam(required = false) String accountType) {
        List<TierDefinitionResponse> definitions = loyaltyService.getTierDefinitions(accountType);
        return ResponseEntity.ok(ApiResponse.success("Tier definitions retrieved", definitions));
    }

    // PUT /api/v1/admin/tier-definitions/{tierId}
    @PutMapping("/tier-definitions/{tierId}")
    public ResponseEntity<ApiResponse<TierDefinitionResponse>> updateTierDefinition(
            @PathVariable Long tierId,
            @Valid @RequestBody TierDefinitionResponse request) {
        TierDefinitionResponse response = loyaltyService.updateTierDefinition(tierId, request);
        return ResponseEntity.ok(ApiResponse.success("Tier definition updated", response));
    }

    // PUT /api/v1/admin/users/{userId}/tier
    @PutMapping("/users/{userId}/tier")
    public ResponseEntity<ApiResponse<TierAdjustmentResponse>> adjustUserTier(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody AdjustTierRequest request) {
        Long adminId = extractUserId(authorizationHeader);
        TierAdjustmentResponse response = loyaltyService.adjustUserTier(adminId, userId, request.getTier(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("User tier adjusted", response));
    }

    // GET /api/v1/admin/users/{userId}/points-ledger
    @GetMapping("/users/{userId}/points-ledger")
    public ResponseEntity<ApiResponse<Page<PointsLedgerResponse>>> getUserPointsLedger(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PointsLedgerResponse> ledger = loyaltyService.getUserPointsLedger(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Points ledger retrieved", ledger));
    }

    // POST /api/v1/admin/users/{userId}/points
    @PostMapping("/users/{userId}/points")
    public ResponseEntity<ApiResponse<PointsLedgerResponse>> addUserPoints(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody AddPointsRequest request) {
        Long adminId = extractUserId(authorizationHeader);
        PointsLedgerResponse response = loyaltyService.addPointsManually(adminId, userId, request.getPoints(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Points added successfully", response));
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
