package com.hotelbooking.ai.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
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
public class AiAdviceRequest {
    @NotNull(message = "Group size is required")
    @Min(1)
    private Integer groupSize;

    @NotNull(message = "Room count is required")
    @Min(1)
    private Integer roomCount;

    @NotNull(message = "Room price is required")
    private BigDecimal roomPrice;

    @NotNull(message = "Meal count is required")
    @Min(1)
    private Integer mealCount;

    @NotNull(message = "Meal price is required")
    private BigDecimal mealPrice;
}
