package com.hotelbooking.wallet.topup;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.wallet.topup.dto.GlobalTopUpLimitsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminTopUpController {

    private final TopUpService topUpService;

    // PUT /api/v1/admin/topup-limits
    @PutMapping("/topup-limits")
    public ResponseEntity<ApiResponse<String>> updateGlobalTopUpLimits(
            @Valid @RequestBody GlobalTopUpLimitsRequest request) {
        topUpService.updateGlobalLimits(request.getMaxSingleTopUp(), request.getMaxWalletBalance());
        return ResponseEntity.ok(ApiResponse.success(
                "Global top-up limits updated: maxSingleTopUp=" + request.getMaxSingleTopUp() +
                ", maxWalletBalance=" + request.getMaxWalletBalance(), null));
    }
}
