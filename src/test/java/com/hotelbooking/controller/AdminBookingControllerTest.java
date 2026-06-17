package com.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.dto.request.UpdateBookingStatusRequest;
import com.hotelbooking.dto.response.AdminBookingResponse;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.JwtAuthenticationFilter;
import com.hotelbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void processBooking_Confirm_Success() throws Exception {
        UpdateBookingStatusRequest request = new UpdateBookingStatusRequest("CONFIRMED");

        AdminBookingResponse response = new AdminBookingResponse(
                10L,
                "BK-TEST",
                "user@example.com",
                "Grand Hotel",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(2),
                BigDecimal.valueOf(200.0),
                "CONFIRMED",
                "BANK_TRANSFER",
                "COMPLETED",
                LocalDateTime.now()
        );

        when(bookingService.processBooking(eq(10L), any(UpdateBookingStatusRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/bookings/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETED"));
    }
}
