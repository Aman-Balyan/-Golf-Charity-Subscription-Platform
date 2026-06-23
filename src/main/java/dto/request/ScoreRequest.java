package dto.request;


import jakarta.validation.constraints.*;
        import lombok.Data;
import java.time.LocalDate;

@Data
public class ScoreRequest {

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 45, message = "Score cannot exceed 45")
    private Integer score;

    @NotNull(message = "Played date is required")
    @PastOrPresent(message = "Played date cannot be in the future")
    private LocalDate playedDate;
}