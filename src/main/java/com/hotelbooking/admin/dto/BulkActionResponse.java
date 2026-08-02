package com.hotelbooking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkActionResponse {
    private Long bulkActionId;
    private String actionType;
    private int affectedCount;
    private String message;
}
