package com.hotelbooking.booking;
import com.hotelbooking.booking.dto.AdminBookingResponse;
import com.hotelbooking.booking.dto.UpdateBookingStatusRequest;
import com.hotelbooking.booking.dto.AdminCreateBookingRequest;
import com.hotelbooking.booking.dto.AdminUpdateBookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.common.security.JwtAuthenticationFilter;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.user.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserRepository userRepository;

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

    @Test
    void adminCreateBooking_Success() throws Exception {
        AdminCreateBookingRequest request = AdminCreateBookingRequest.builder()
                .userId(1L)
                .hotelId(2L)
                .checkInDate(java.time.LocalDate.now().plusDays(1))
                .checkOutDate(java.time.LocalDate.now().plusDays(3))
                .roomIds(java.util.List.of(101L))
                .paymentMethod("ONLINE")
                .build();

        BookingResponse response = BookingResponse.builder()
                .bookingId(12L)
                .bookingCode("BK-ADMIN-NEW")
                .userId(1L)
                .hotelId(2L)
                .totalAmount(BigDecimal.valueOf(150.0))
                .status("PENDING")
                .roomIds(java.util.List.of(101L))
                .build();

        when(bookingService.adminCreateBooking(any(AdminCreateBookingRequest.class))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/admin/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.bookingCode").value("BK-ADMIN-NEW"));
    }

    @Test
    void adminUpdateBooking_Success() throws Exception {
        Long bookingId = 12L;
        AdminUpdateBookingRequest request = AdminUpdateBookingRequest.builder()
                .status("CONFIRMED")
                .build();

        BookingResponse response = BookingResponse.builder()
                .bookingId(bookingId)
                .bookingCode("BK-ADMIN-NEW")
                .status("CONFIRMED")
                .build();

        when(bookingService.adminUpdateBooking(eq(bookingId), any(AdminUpdateBookingRequest.class))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/bookings/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void adminDeleteBooking_Success() throws Exception {
        Long bookingId = 12L;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/admin/bookings/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Booking deleted successfully by Admin"));
    }
}
