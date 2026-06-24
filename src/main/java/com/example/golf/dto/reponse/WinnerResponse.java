package com.example.golf.dto.reponse;


import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class WinnerResponse {
    private UUID id;
    private UUID drawId;
    private UUID userId;
    private String userFullName;
    private String matchType;
    private BigDecimal prizeAmount;
    private String verificationStatus;
    private String payoutStatus;
    private String proofUrl;
    private LocalDateTime createdAt;
}