package com.hotelbooking.user;

import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.user.dto.CreateUserRequest;
import com.hotelbooking.user.dto.UpdateUserRequest;
import com.hotelbooking.user.dto.UpdateUserStatusRequest;
import com.hotelbooking.user.dto.UserResponse;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.hotel.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Admin creating user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists: " + request.getEmail());
        }

        String role = request.getRole() != null ? request.getRole().toUpperCase() : "CUSTOMER";
        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE";

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(role)
                .status(status)
                .failedLoginAttempts(0)
                .phoneNumber(request.getPhoneNumber())
                .identificationNumber(request.getIdentificationNumber())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Successfully created user with ID: {}", savedUser.getUserId());
        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        log.info("Admin updating user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailAndUserIdNot(request.getEmail(), userId)) {
                throw new BusinessException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole().toUpperCase());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus().toUpperCase());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getIdentificationNumber() != null) {
            user.setIdentificationNumber(request.getIdentificationNumber());
        }

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated user ID: {}", userId);
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Admin deleting user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        long bookingsCount = bookingRepository.countByUser_UserId(userId);
        if (bookingsCount > 0) {
            throw new BusinessException("Cannot delete user as they have associated bookings.");
        }

        // Delete reviews first to satisfy foreign key constraints
        reviewRepository.deleteByUser_UserId(userId);

        userRepository.delete(user);
        log.info("Successfully deleted user ID: {}", userId);
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