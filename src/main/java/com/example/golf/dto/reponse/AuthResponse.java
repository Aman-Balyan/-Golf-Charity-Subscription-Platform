package com.example.golf.dto.reponse;

import lombok.*;
import java.util.UUID;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UUID userId;
    private String email;
    private String fullName;
    private String role;
}