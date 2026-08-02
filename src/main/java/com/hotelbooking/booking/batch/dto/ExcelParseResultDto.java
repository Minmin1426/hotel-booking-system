package com.hotelbooking.booking.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelParseResultDto {
    @Builder.Default
    private int totalRows = 0;
    @Builder.Default
    private int validRows = 0;
    @Builder.Default
    private int invalidRows = 0;
    @Builder.Default
    private List<ExcelRowErrorDto> errors = new ArrayList<>();
    private Long blockBookingId;
}
