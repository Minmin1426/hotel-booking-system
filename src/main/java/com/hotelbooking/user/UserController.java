package com.hotelbooking.user;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.user.ctp.CtpService;
import com.hotelbooking.user.dto.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final CtpService ctpService;
    private final JwtService jwtService;

    @PutMapping("/me/profile")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR', 'RECEPTIONIST', 'HOUSEKEEPER')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateProfileRequest request) {

        Long userId = extractUserIdFromToken(authorizationHeader);
        UserProfileResponse updated = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @GetMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @RequestHeader("Authorization") String authorizationHeader) {

        Long userId = extractUserIdFromToken(authorizationHeader);
        UserProfileResponse profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    // 007-customer-portal-profile: Corporate Tax Profile endpoints

    @GetMapping("/me/corporate-profile")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'DIRECTOR')")
    public ResponseEntity<ApiResponse<CorporateProfileResponse>> getCorporateProfile(
            @RequestHeader("Authorization") String authorizationHeader) {

        Long userId = extractUserIdFromToken(authorizationHeader);
        CorporateProfileResponse profile = ctpService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Corporate profile retrieved", profile));
    }

    @PutMapping("/me/corporate-profile")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'DIRECTOR')")
    public ResponseEntity<ApiResponse<CorporateProfileResponse>> submitCorporateProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CorporateProfileRequest request) {

        Long userId = extractUserIdFromToken(authorizationHeader);
        CorporateProfileResponse response = ctpService.submitProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    private Long extractUserIdFromToken(String authorizationHeader) {
        String token = authorizationHeader.substring(7); // strip "Bearer "
        return jwtService.extractUserId(token);
    }
}
