package entity;


import jakarta.persistence.*;
        import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "draws")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Draw {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "draw_month", nullable = false)
    private Integer drawMonth;

    @Column(name = "draw_year", nullable = false)
    private Integer drawYear;

    @Column(name = "draw_type")
    private String drawType; // RANDOM / ALGORITHMIC

    @Column(name = "status")
    private String status; // PENDING / SIMULATED / PUBLISHED

    @Column(name = "winning_numbers", columnDefinition = "integer[]")
    private int[] winningNumbers;

    @Column(name = "jackpot_amount")
    private BigDecimal jackpotAmount = BigDecimal.ZERO;

    @Column(name = "total_pool")
    private BigDecimal totalPool = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
        if (drawType == null) drawType = "RANDOM";
        if (jackpotAmount == null) jackpotAmount = BigDecimal.ZERO;
        if (totalPool == null) totalPool = BigDecimal.ZERO;
    }
}