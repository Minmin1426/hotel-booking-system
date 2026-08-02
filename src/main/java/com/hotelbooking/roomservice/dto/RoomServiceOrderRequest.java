package com.hotelbooking.roomservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RoomServiceOrderRequest {
    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Item is required")
    private String item;

    @NotNull(message = "Quantity is required")
    @Min(1)
    private Integer quantity;

    private String notes;
}
