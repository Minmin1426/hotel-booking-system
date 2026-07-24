package com.hotelbooking.roomservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomServiceOrderResponse {
    private Long id;
    private String roomNumber;
    private String item;
    private Integer quantity;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
}
