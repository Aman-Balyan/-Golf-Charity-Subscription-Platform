package dto.request;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProofUploadRequest {
    @NotBlank(message = "Proof URL is required")
    private String proofUrl;
}