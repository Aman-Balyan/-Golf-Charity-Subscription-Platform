package com.example.golf.dto.reponse;


import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class UserDetailResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String role;
    private UUID charityId;
    private String charityName;
    private Integer charityPercentage;
    private LocalDateTime createdAt;
    private SubscriptionResponse activeSubscription;
    private List<ScoreResponse> scores;
    private BigDecimal totalDonated;
    private List<WinnerResponse> winnings;
}