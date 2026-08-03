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

    public AiAdviceResponse() {}
    public AiAdviceResponse(String recommendation, List<String> suggestions, BigDecimal estimatedRoomCost, BigDecimal estimatedMealCost, BigDecimal estimatedTotal) {
        this.recommendation = recommendation;
        this.suggestions = suggestions;
        this.estimatedRoomCost = estimatedRoomCost;
        this.estimatedMealCost = estimatedMealCost;
        this.estimatedTotal = estimatedTotal;
    }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    public BigDecimal getEstimatedRoomCost() { return estimatedRoomCost; }
    public void setEstimatedRoomCost(BigDecimal estimatedRoomCost) { this.estimatedRoomCost = estimatedRoomCost; }
    public BigDecimal getEstimatedMealCost() { return estimatedMealCost; }
    public void setEstimatedMealCost(BigDecimal estimatedMealCost) { this.estimatedMealCost = estimatedMealCost; }
    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }
}
