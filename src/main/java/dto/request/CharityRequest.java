package dto.request;


import jakarta.validation.constraints.*;
        import lombok.Data;

@Data
public class CharityRequest {
    @NotBlank(message = "Charity name is required")
    private String name;

    private String description;
    private String imageUrl;
    private String websiteUrl;
    private Boolean isFeatured = false;
}