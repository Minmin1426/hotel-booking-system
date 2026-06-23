package com.hotelbooking.user;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.user.dto.CreateUserRequest;
import com.hotelbooking.user.dto.UpdateUserRequest;
import com.hotelbooking.user.dto.UpdateUserStatusRequest;
import com.hotelbooking.user.dto.UserResponse;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.hotel.ReviewRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFullName("Test User");
        mockUser.setRole("ROLE_USER");
        mockUser.setStatus("ACTIVE");
        mockUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllUsers_ShouldReturnPaginatedUsers() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(mockUser));
        when(userRepository.findAll(pageRequest)).thenReturn(userPage);

        // Act
        Page<UserResponse> result = adminUserService.getAllUsers(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mockUser.getEmail(), result.getContent().get(0).email());
        verify(userRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void updateUserStatus_WhenUserExists_ShouldUpdateAndReturnResponse() {
        // Arrange
        Long userId = 1L;
        UpdateUserStatusRequest request = new UpdateUserStatusRequest("LOCKED");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = adminUserService.updateUserStatus(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("LOCKED", result.status());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateUserStatus_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        Long userId = 99L;
        UpdateUserStatusRequest request = new UpdateUserStatusRequest("LOCKED");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            adminUserService.updateUserStatus(userId, request)
        );
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WithNewEmail_ShouldHashPasswordAndSave() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .email("new@example.com")
                .fullName("New User")
                .password("plainPassword")
                .role("CUSTOMER")
                .status("ACTIVE")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(2L);
            return u;
        });

        // Act
        UserResponse response = adminUserService.createUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.userId());
        assertEquals("new@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowBusinessException() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> adminUserService.createUser(request));
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WithValidFields_ShouldUpdateAndSave() {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("New Full Name")
                .password("newPassword")
                .role("STAFF")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse response = adminUserService.updateUser(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals("New Full Name", response.fullName());
        assertEquals("STAFF", response.role());
        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void deleteUser_WhenNoBookings_ShouldDeleteUserAndReviews() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(bookingRepository.countByUser_UserId(userId)).thenReturn(0L);

        // Act
        adminUserService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).countByUser_UserId(userId);
        verify(reviewRepository, times(1)).deleteByUser_UserId(userId);
        verify(userRepository, times(1)).delete(mockUser);
    }

    @Test
    void deleteUser_WhenHasBookings_ShouldThrowBusinessException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(bookingRepository.countByUser_UserId(userId)).thenReturn(5L);

        // Act & Assert
        assertThrows(BusinessException.class, () -> adminUserService.deleteUser(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).countByUser_UserId(userId);
        verify(reviewRepository, never()).deleteByUser_UserId(anyLong());
        verify(userRepository, never()).delete(any());
    }
}
