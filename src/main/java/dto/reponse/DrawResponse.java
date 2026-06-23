package dto.reponse;



import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class DrawResponse {
    private UUID id;
    private Integer drawMonth;
    private Integer drawYear;
    private String drawType;
    private String status;
    private int[] winningNumbers;
    private BigDecimal jackpotAmount;
    private BigDecimal totalPool;
    private LocalDateTime publishedAt;
    private List<WinnerResponse> winners;
}