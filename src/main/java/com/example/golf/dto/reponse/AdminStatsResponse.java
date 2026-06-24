package com.example.golf.dto.reponse;


import lombok.*;
        import java.math.BigDecimal;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long activeSubscribers;
    private BigDecimal totalPrizePool;
    private BigDecimal totalCharityContributions;
    private long totalDrawsPublished;
    private long pendingVerifications;
    private long totalWinners;
}