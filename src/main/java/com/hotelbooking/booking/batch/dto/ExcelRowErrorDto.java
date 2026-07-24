package com.hotelbooking.booking.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelRowErrorDto {
    private int rowNumber;
    private String field;
    private String message;
}
