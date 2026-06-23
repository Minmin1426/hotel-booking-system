package com.hotelbooking.user;
import com.hotelbooking.common.config.SecurityConfig;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.common.security.TokenBlacklistService;
import com.hotelbooking.user.dto.CreateUserRequest;
import com.hotelbooking.user.dto.UpdateUserRequest;
import com.hotelbooking.user.dto.UpdateUserStatusRequest;
import com.hotelbooking.user.dto.UserResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithAdminRole_ShouldReturn200Ok() throws Exception {
        // Arrange
        UserResponse response = new UserResponse(1L, "test@email.com", "Name", "ROLE_USER", "ACTIVE", LocalDateTime.now());
        when(adminUserService.getAllUsers(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("test@email.com"));
    }

    @Test
    @WithMockUser(roles = "USER") // Wrong role
    void getAllUsers_WithUserRole_ShouldReturn403Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatus_WithValidRequest_ShouldReturn200Ok() throws Exception {
        // Arrange
        Long userId = 1L;
        UpdateUserStatusRequest request = new UpdateUserStatusRequest("LOCKED");
        UserResponse response = new UserResponse(userId, "test@email.com", "Name", "ROLE_USER", "LOCKED", LocalDateTime.now());
        
        when(adminUserService.updateUserStatus(eq(userId), any(UpdateUserStatusRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/admin/users/{userId}/status", userId)
                .with(csrf()) // Required for CSRF in spring security test
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatus_WithInvalidStatusValue_ShouldReturn400BadRequest() throws Exception {
        // Arrange - Invalid status (not ACTIVE or LOCKED)
        Long userId = 1L;
        UpdateUserStatusRequest request = new UpdateUserStatusRequest("INVALID_STATUS");

        // Act & Assert
        mockMvc.perform(patch("/api/v1/admin/users/{userId}/status", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Triggers validation annotation
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WithValidRequest_ShouldReturn201Created() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .email("newuser@example.com")
                .fullName("New User")
                .password("password123")
                .role("CUSTOMER")
                .status("ACTIVE")
                .build();
        UserResponse response = new UserResponse(2L, "newuser@example.com", "New User", "CUSTOMER", "ACTIVE", LocalDateTime.now());

        when(adminUserService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/admin/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("New User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WithValidRequest_ShouldReturn200Ok() throws Exception {
        Long userId = 1L;
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Updated Name")
                .build();
        UserResponse response = new UserResponse(userId, "test@email.com", "Updated Name", "ROLE_USER", "ACTIVE", LocalDateTime.now());

        when(adminUserService.updateUser(eq(userId), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/users/{userId}", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturn200Ok() throws Exception {
        Long userId = 1L;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/admin/users/{userId}", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }
}
