package com.hotelbooking.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String paymentUrl;
    private String transactionId;
    private String clientSecret;
    
    // Bank transfer details
    private String bankName;
    private String accountHolder;
    private String accountNumber;
    private String referenceCode;
    private String branch;
    private String swiftCode;
}
