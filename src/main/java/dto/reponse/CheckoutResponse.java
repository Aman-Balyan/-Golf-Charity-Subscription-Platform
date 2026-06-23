package dto.reponse;


import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class CheckoutResponse {
    private String clientSecret;
    private String subscriptionId;
}