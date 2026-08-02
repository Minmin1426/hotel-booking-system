package com.hotelbooking.mealticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueMealTicketRequest {
    @NotNull
    private Long userId;

    @NotNull
    private String ticketType;

    private Integer validDays;
    private String notes;
}
