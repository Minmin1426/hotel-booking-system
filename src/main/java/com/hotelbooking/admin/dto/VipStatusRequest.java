package com.hotelbooking.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VipStatusRequest {
    @NotNull(message = "isVip is required")
    private Boolean isVip;
}
