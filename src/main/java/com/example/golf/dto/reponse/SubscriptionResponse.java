package com.example.golf.dto.reponse;


import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class SubscriptionResponse {
    private UUID id;
    private String plan;
    private String status;
    private BigDecimal amount;
    private LocalDateTime startDate;
    private LocalDateTime renewalDate;
    private LocalDateTime endDate;
    private String stripeSubscriptionId;
}