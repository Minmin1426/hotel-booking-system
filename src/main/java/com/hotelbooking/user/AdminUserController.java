package com.hotelbooking.user;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.user.ctp.CtpService;
import com.hotelbooking.user.ctp.dto.*;
import com.hotelbooking.user.dto.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final CtpService ctpService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = adminUserService.createUser(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = adminUserService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserResponse updatedUser = adminUserService.updateUserStatus(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    // 007-customer-portal-profile: CTP Verification Admin endpoints

    @GetMapping("/ctp-verifications")
    public ResponseEntity<Page<CtpVerificationSummary>> listCtpVerifications(
            @RequestParam(required = false) String ctpStatus,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CtpVerificationSummary> verifications = ctpService.listVerifications(ctpStatus, pageable);
        return ResponseEntity.ok(verifications);
    }

    @PostMapping("/{userId}/ctp/approve")
    public ResponseEntity<ApiResponse<CorporateProfileResponse>> approveCtp(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody(required = false) ApproveCtpRequest request) {
        Long adminId = extractAdminId(authorizationHeader);
        CorporateProfileResponse response = ctpService.approveProfile(userId, adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Corporate tax profile approved", response));
    }

    @PostMapping("/{userId}/ctp/reject")
    public ResponseEntity<ApiResponse<CorporateProfileResponse>> rejectCtp(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody RejectCtpRequest request) {
        Long adminId = extractAdminId(authorizationHeader);
        CorporateProfileResponse response = ctpService.rejectProfile(userId, adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Corporate tax profile rejected", response));
    }

    private Long extractAdminId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}