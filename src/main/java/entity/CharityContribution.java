package entity;


import jakarta.persistence.*;
        import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "charity_contributions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CharityContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "charity_id", nullable = false)
    private UUID charityId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}