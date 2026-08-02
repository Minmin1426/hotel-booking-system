package com.hotelbooking.room.dto;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.room.Room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotBlank(message = "Room type is required")
    private String roomType;
}
