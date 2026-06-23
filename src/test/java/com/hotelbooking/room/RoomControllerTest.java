package com.hotelbooking.room;
import com.hotelbooking.common.security.JwtAuthenticationFilter;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.room.dto.RoomAvailabilityResponse;
import com.hotelbooking.room.dto.RoomSearchRequest;
import com.hotelbooking.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserRepository userRepository;

    @Test
    void searchAvailableRooms_Success_ReturnsList() throws Exception {
        RoomAvailabilityResponse response = RoomAvailabilityResponse.builder()
                .roomId(101L)
                .roomNumber("101")
                .roomType("SINGLE")
                .pricePerNight(BigDecimal.valueOf(500.0))
                .status("AVAILABLE")
                .hotelId(1L)
                .hotelName("Grand Hotel")
                .build();

        when(roomService.findAvailableRooms(any(RoomSearchRequest.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/rooms/search")
                        .param("hotelId", "1")
                        .param("checkIn", LocalDate.now().plusDays(1).toString())
                        .param("checkOut", LocalDate.now().plusDays(3).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Available rooms retrieved successfully"))
                .andExpect(jsonPath("$.data[0].roomId").value(101L))
                .andExpect(jsonPath("$.data[0].roomNumber").value("101"))
                .andExpect(jsonPath("$.data[0].pricePerNight").value(500.0));
    }

    @Test
    void searchAvailableRooms_MissingParams_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/rooms/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // validation fails
    }
}
