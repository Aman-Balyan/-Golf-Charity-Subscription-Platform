package com.example.golf.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.golf.service.SubscriptionService;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final SubscriptionService subscriptionService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        switch (event.getType()) {
            case "invoice.payment_succeeded" -> {
                var subscription = (Subscription) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (subscription != null) {
                    subscriptionService.handleWebhookPaymentSuccess(subscription.getId());
                }
            }
            case "invoice.payment_failed" -> {
                var subscription = (Subscription) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (subscription != null) {
                    subscriptionService.handleWebhookPaymentFailed(subscription.getId());
                }
            }
        }

        return ResponseEntity.ok("Received");
    }
}