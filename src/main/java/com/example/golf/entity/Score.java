package com.example.golf.entity;



import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "played_date", nullable = false)
    private LocalDate playedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}