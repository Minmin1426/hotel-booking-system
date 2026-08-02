package com.hotelbooking.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupAllocationRequestDto {
    private Long hotelId;
    private Long bookingId;
    private String groupName;
    private List<Long> roomMatrixStateIds;
}
