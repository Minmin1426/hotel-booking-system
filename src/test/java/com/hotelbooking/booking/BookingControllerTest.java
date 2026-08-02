package com.hotelbooking.booking;
import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.dto.DateValidationRequest;
import com.hotelbooking.booking.dto.DateValidationResponse;
import com.hotelbooking.common.security.JwtAuthenticationFilter;
import com.hotelbooking.user.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

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
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void validateDates_Success_ReturnsValidResponse() throws Exception {
        DateValidationRequest request = DateValidationRequest.builder()
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .build();

        DateValidationResponse response = DateValidationResponse.builder()
                .valid(true)
                .nights(2)
                .message("Dates are valid")
                .build();

        when(bookingService.validateDates(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings/validate-dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.nights").value(2));
    }

    @Test
    void createBooking_Success_ReturnsCreatedResponse() throws Exception {
        BookingRequest request = BookingRequest.builder()
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .roomIds(List.of(101L))
                .adults(2)
                .children(0)
                .build();

        BookingResponse response = BookingResponse.builder()
                .bookingId(10L)
                .bookingCode("BK-123")
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .totalAmount(BigDecimal.valueOf(1000.0))
                .status("PENDING")
                .roomIds(List.of(101L))
                .build();

        when(bookingService.createBooking(any(BookingRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.bookingCode").value("BK-123"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
