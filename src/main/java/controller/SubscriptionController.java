package controller;

import dto.reponse.CheckoutResponse;
import dto.reponse.SubscriptionResponse;
import dto.request.SubscriptionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import service.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // Create subscription + get Stripe client secret
    @PostMapping
    public ResponseEntity<CheckoutResponse> createSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(
                subscriptionService.createSubscription(userDetails.getUsername(), request));
    }

    // Cancel active subscription
    @DeleteMapping
    public ResponseEntity<Void> cancelSubscription(
            @AuthenticationPrincipal UserDetails userDetails) {
        subscriptionService.cancelSubscription(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Get current active subscription
    @GetMapping
    public ResponseEntity<SubscriptionResponse> getMySubscription(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                subscriptionService.getMySubscription(userDetails.getUsername()));
    }

    // Get full subscription history
    @GetMapping("/history")
    public ResponseEntity<List<SubscriptionResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                subscriptionService.getMySubscriptionHistory(userDetails.getUsername()));
    }
}