package dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationRequest {
    @NotBlank(message = "Status is required") // APPROVED or REJECTED
    private String status;

    private String rejectionReason;
}