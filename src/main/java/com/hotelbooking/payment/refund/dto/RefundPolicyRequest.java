package com.hotelbooking.payment.refund.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPolicyRequest {

    @NotBlank(message = "Policy name is required")
    @Size(max = 255)
    private String name;

    @NotNull(message = "Days before check-in is required")
    @Min(value = 0, message = "Days before check-in must be >= 0")
    private Integer daysBeforeCheckin;

    @NotNull(message = "Refund percentage is required")
    @DecimalMin(value = "0.00", message = "Refund percentage must be >= 0")
    @DecimalMax(value = "100.00", message = "Refund percentage must be <= 100")
    private BigDecimal refundPercentage;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Priority is required")
    private Integer priority;

    @Builder.Default
    private Boolean isActive = true;
}
