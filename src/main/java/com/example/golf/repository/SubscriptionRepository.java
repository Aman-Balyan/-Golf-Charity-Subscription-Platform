package com.example.golf.repository;

import com.example.golf.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByUserIdAndStatus(UUID userId, String status);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    List<Subscription> findByUserId(UUID userId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE'")
    long countActiveSubscriptions();

    @Query("SELECT SUM(s.amount) FROM Subscription s WHERE s.status = 'ACTIVE'")
    java.math.BigDecimal sumActiveSubscriptionAmounts();
}