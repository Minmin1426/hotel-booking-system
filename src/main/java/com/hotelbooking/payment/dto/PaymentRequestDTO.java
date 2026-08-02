package com.hotelbooking.payment.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private Boolean isDeposit;
    private java.math.BigDecimal depositRatio;

    // VAT Invoice details (Optional)
    private String companyName;
    private String taxId;
    private String companyAddress;
    private String companyEmail;
}
