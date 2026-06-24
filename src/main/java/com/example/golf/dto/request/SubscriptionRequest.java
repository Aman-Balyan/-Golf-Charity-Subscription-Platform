package com.example.golf.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionRequest {
    @NotBlank(message = "Plan is required")
    private String plan; // MONTHLY or YEARLY

    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;
}