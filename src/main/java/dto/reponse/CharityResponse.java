package dto.reponse;


import lombok.*;
        import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class CharityResponse {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private String websiteUrl;
    private Boolean isFeatured;
    private Boolean isActive;
    private BigDecimal totalContributions;
}