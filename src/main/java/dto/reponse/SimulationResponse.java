package dto.reponse;


import lombok.*;
        import java.math.BigDecimal;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class SimulationResponse {
    private int[] winningNumbers;
    private int threeMatchCount;
    private int fourMatchCount;
    private int fiveMatchCount;
    private BigDecimal totalPool;
    private BigDecimal jackpotPool;
    private BigDecimal fourMatchPool;
    private BigDecimal threeMatchPool;
    private BigDecimal jackpotPerWinner;
    private BigDecimal fourMatchPerWinner;
    private BigDecimal threeMatchPerWinner;
    private List<WinnerResponse> projectedWinners;
}