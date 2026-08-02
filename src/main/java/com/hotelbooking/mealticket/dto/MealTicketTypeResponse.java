package com.hotelbooking.mealticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealTicketTypeResponse {
    private Long typeId;
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;
    private Integer defaultValidDays;
    private BigDecimal defaultPrice;
    private Boolean isActive;
}
