package com.hotelbooking.user.ctp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectCtpRequest {

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}
