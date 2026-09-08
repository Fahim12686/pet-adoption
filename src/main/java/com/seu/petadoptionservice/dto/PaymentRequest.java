package com.seu.petadoptionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentRequest {
    private Long applicationId;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card must be 16 digits")
    private String cardNumber;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Format MM/YY")
    private String expiry;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits")
    private String cvv;
}