package com.hotelbooking.ai.dto;

import java.math.BigDecimal;
import java.util.List;

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
public class AiAdviceResponse {
    private String recommendation;
    private List<String> suggestions;
    private BigDecimal estimatedRoomCost;
    private BigDecimal estimatedMealCost;
    private BigDecimal estimatedTotal;
}
