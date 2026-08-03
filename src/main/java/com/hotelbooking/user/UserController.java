package com.hotelbooking.user;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
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
    private final JwtService jwtService;
    private final UserRepository userRepository;

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

    private Long extractUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new com.hotelbooking.common.exception.BusinessException("Authorization token is missing or invalid");
        }
        String token = authorizationHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        if (userId != null) {
            return userId;
        }
        String email = jwtService.extractUsername(token);
        if (email != null) {
            return userRepository.findByEmail(email)
                    .map(User::getUserId)
                    .orElseThrow(() -> new com.hotelbooking.common.exception.ResourceNotFoundException("User not found for email: " + email));
        }
        throw new com.hotelbooking.common.exception.BusinessException("Invalid user claims in token");
    }
}
