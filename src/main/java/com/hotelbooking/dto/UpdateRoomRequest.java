package com.hotelbooking.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoomRequest {

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private String roomType;
}
