package com.hotelbooking.loyalty;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.loyalty.dto.TierHistoryResponse;
import com.hotelbooking.loyalty.dto.TierInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final JwtService jwtService;

    // GET /api/v1/users/me/tier
    @GetMapping("/tier")
    public ResponseEntity<ApiResponse<TierInfoResponse>> getMyTier(
            @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = extractUserId(authorizationHeader);
        TierInfoResponse response = loyaltyService.getMyTier(userId);
        return ResponseEntity.ok(ApiResponse.success("Tier information retrieved", response));
    }

    // GET /api/v1/users/me/tier/history
    @GetMapping("/tier/history")
    public ResponseEntity<ApiResponse<Page<TierHistoryResponse>>> getMyTierHistory(
            @RequestHeader("Authorization") String authorizationHeader,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = extractUserId(authorizationHeader);
        Page<TierHistoryResponse> response = loyaltyService.getMyTierHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tier history retrieved", response));
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
