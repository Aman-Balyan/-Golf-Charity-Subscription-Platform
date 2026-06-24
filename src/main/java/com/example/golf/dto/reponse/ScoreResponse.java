package com.example.golf.dto.reponse;


import lombok.*;
        import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class ScoreResponse {
    private UUID id;
    private Integer score;
    private LocalDate playedDate;
    private LocalDateTime createdAt;
}