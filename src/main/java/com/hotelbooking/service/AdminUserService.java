package com.hotelbooking.service;

import com.hotelbooking.dto.request.UpdateUserStatusRequest;
import com.hotelbooking.dto.response.UserResponse;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.model.User;
import com.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        log.info("Admin requested to change status of user ID: {} to {}", userId, request.status());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User ID: {} not found", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        user.setStatus(request.status());
        User updatedUser = userRepository.save(user);
        
        log.info("Successfully updated status of user ID: {}", userId);
        return mapToResponse(updatedUser);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}