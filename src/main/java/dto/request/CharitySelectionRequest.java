package dto.request;


import jakarta.validation.constraints.*;
        import lombok.Data;
import java.util.UUID;

@Data
public class CharitySelectionRequest {

    @NotNull(message = "Charity ID is required")
    private UUID charityId;

    @Min(value = 10, message = "Minimum charity percentage is 10%")
    @Max(value = 100, message = "Maximum charity percentage is 100%")
    private Integer charityPercentage = 10;
}