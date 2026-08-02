package com.hotelbooking.mealticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkIssueRequest {
    @NotBlank
    private String ticketType;

    private Integer validDays;

    @NotEmpty
    private List<Long> memberIds;
}
