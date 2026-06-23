package com.hotelbooking.hotel;
import com.hotelbooking.common.security.JwtAuthenticationFilter;
import com.hotelbooking.hotel.dto.HotelDetailResponse;
import com.hotelbooking.hotel.dto.HotelResponse;
import com.hotelbooking.hotel.dto.HotelSearchResponseDTO;
import com.hotelbooking.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
@AutoConfigureMockMvc(addFilters = false) // Isolate controller layer logic
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserRepository userRepository;

    @Test
    void searchHotels_Success_ReturnsPaginatedList() throws Exception {
        HotelSearchResponseDTO hotelDTO = HotelSearchResponseDTO.builder()
                .hotelId(1L)
                .name("InterContinental Hanoi")
                .location("Hanoi")
                .description("Luxurious")
                .images(Collections.emptyList())
                .build();

        Page<HotelSearchResponseDTO> pageResponse = new PageImpl<>(
                Collections.singletonList(hotelDTO),
                Pageable.ofSize(20),
                1
        );

        when(hotelService.searchHotels(eq("Hanoi"), any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/hotels/search")
                        .param("location", "Hanoi")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].hotelId").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("InterContinental Hanoi"))
                .andExpect(jsonPath("$.content[0].location").value("Hanoi"))
                .andExpect(jsonPath("$.content[0].description").value("Luxurious"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(hotelService).searchHotels(eq("Hanoi"), any(Pageable.class));
    }

    @Test
    void searchHotels_OverLimitPageSize_EnforcesLimit() throws Exception {
        Page<HotelSearchResponseDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(hotelService.searchHotels(any(), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/hotels/search")
                        .param("location", "Hanoi")
                        .param("page", "0")
                        .param("size", "100") // Over BR-03 limit
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(hotelService).searchHotels(eq("Hanoi"), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageSize()).isEqualTo(20); // Clamped to 20
        assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
    }

    @Test
    void searchHotels_InvalidPageAndSize_ClampsToValidDefaults() throws Exception {
        Page<HotelSearchResponseDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(hotelService.searchHotels(any(), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/hotels/search")
                        .param("location", "Saigon")
                        .param("page", "-5")
                        .param("size", "-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(hotelService).searchHotels(eq("Saigon"), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(0); // Clamped to 0
        assertThat(capturedPageable.getPageSize()).isEqualTo(20);  // Defaulted to 20
    }

    @Test
    void getHotels_Success() throws Exception {
        HotelResponse hotelResponse = HotelResponse.builder()
                .hotelId(1L)
                .name("Sheraton Hanoi")
                .location("Hanoi")
                .isActive(true)
                .minPrice(100.0)
                .build();

        when(hotelService.getHotels(any())).thenReturn(Collections.singletonList(hotelResponse));

        mockMvc.perform(get("/api/v1/hotels")
                        .param("location", "Hanoi")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hotelId").value(1L))
                .andExpect(jsonPath("$[0].name").value("Sheraton Hanoi"))
                .andExpect(jsonPath("$[0].minPrice").value(100.0));
    }

    @Test
    void getHotelDetail_Success() throws Exception {
        HotelDetailResponse detailResponse = HotelDetailResponse.builder()
                .hotelId(1L)
                .name("Sheraton Hanoi")
                .location("Hanoi")
                .description("Nice hotel")
                .rating(4.5)
                .images(Collections.emptyList())
                .build();

        when(hotelService.getHotelDetail(1L)).thenReturn(detailResponse);

        mockMvc.perform(get("/api/v1/hotels/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId").value(1L))
                .andExpect(jsonPath("$.name").value("Sheraton Hanoi"))
                .andExpect(jsonPath("$.rating").value(4.5));
    }
}
