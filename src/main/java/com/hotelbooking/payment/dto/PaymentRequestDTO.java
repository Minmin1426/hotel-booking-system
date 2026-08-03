package com.hotelbooking.payment.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^(?i)(CASH|VNPAY|CARD|STRIPE|ONLINE)$", message = "Payment method must be CASH, VNPAY, or CARD")
    private String paymentMethod;

    private Boolean isDeposit;
    private java.math.BigDecimal depositRatio;

    // VAT Invoice details (Optional)
    private String companyName;
    private String taxId;
    private String companyAddress;
    private String companyEmail;
}
