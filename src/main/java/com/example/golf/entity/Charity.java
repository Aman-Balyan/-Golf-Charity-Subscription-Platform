package com.example.golf.entity;

import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "charities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Charity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isFeatured == null) isFeatured = false;
        if (isActive == null) isActive = true;
    }
}