package com.example.golf.entity;


import jakarta.persistence.*;
        import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "winners")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Winner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "draw_id", nullable = false)
    private UUID drawId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "match_type", nullable = false)
    private String matchType; // THREE_MATCH / FOUR_MATCH / FIVE_MATCH

    @Column(name = "prize_amount", nullable = false)
    private BigDecimal prizeAmount;

    @Column(name = "verification_status")
    private String verificationStatus; // PENDING / APPROVED / REJECTED

    @Column(name = "proof_url")
    private String proofUrl;

    @Column(name = "payout_status")
    private String payoutStatus; // PENDING / PAID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (verificationStatus == null) verificationStatus = "PENDING";
        if (payoutStatus == null) payoutStatus = "PENDING";
    }
}