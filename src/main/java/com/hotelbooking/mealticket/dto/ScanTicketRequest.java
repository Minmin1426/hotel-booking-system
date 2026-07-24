package com.hotelbooking.mealticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanTicketRequest {
    @NotBlank
    private String qrCode;
}
