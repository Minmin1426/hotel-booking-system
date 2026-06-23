package com.hotelbooking.user;
import com.hotelbooking.user.dto.CreateUserRequest;
import com.hotelbooking.user.dto.UpdateUserRequest;
import com.hotelbooking.user.dto.UpdateUserStatusRequest;
import com.hotelbooking.user.dto.UserResponse;

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
    public ResponseEntity<com.hotelbooking.common.dto.ApiResponse<Void>> deleteUser(
            @PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(com.hotelbooking.common.dto.ApiResponse.success("User deleted successfully", null));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserResponse updatedUser = adminUserService.updateUserStatus(userId, request);
        return ResponseEntity.ok(updatedUser);
    }
}