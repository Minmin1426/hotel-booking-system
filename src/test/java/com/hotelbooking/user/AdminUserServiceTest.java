package com.hotelbooking.user;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.user.dto.UpdateUserStatusRequest;
import com.hotelbooking.user.dto.UserResponse;

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
}
