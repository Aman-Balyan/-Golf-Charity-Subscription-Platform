package com.example.golf.service;


import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import com.example.golf.dto.reponse.CheckoutResponse;
import com.example.golf.dto.reponse.SubscriptionResponse;
import com.example.golf.dto.request.SubscriptionRequest;
import com.example.golf.entity.CharityContribution;
import com.example.golf.entity.Subscription;
import com.example.golf.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.golf.repository.CharityContributionRepository;
import com.example.golf.repository.SubscriptionRepository;
import com.example.golf.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CharityContributionRepository contributionRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.price.monthly}")
    private String monthlyPriceId;

    @Value("${stripe.price.yearly}")
    private String yearlyPriceId;

    @Value("${stripe.price.monthly.amount}")
    private BigDecimal monthlyAmount;

    @Value("${stripe.price.yearly.amount}")
    private BigDecimal yearlyAmount;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Transactional
    public CheckoutResponse createSubscription(String email, SubscriptionRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check no active subscription already exists
        Optional<Subscription> existing = subscriptionRepository
                .findByUserIdAndStatus(user.getId(), "ACTIVE");
        if (existing.isPresent()) {
            throw new RuntimeException("User already has an active subscription");
        }

        try {

            Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .setEmail(user.getEmail())
                            .setName(user.getFullName())
                            .build()
            );


            PaymentMethod paymentMethod = PaymentMethod.retrieve(request.getPaymentMethodId());
            paymentMethod.attach(
                    PaymentMethodAttachParams.builder()
                            .setCustomer(customer.getId())
                            .build()
            );

            com.stripe.param.CustomerUpdateParams customerUpdateParams =
                    com.stripe.param.CustomerUpdateParams.builder()
                            .setInvoiceSettings(
                                    com.stripe.param.CustomerUpdateParams.InvoiceSettings.builder()
                                            .setDefaultPaymentMethod(request.getPaymentMethodId())
                                            .build()
                            ).build();
            customer.update(customerUpdateParams);


            String priceId = request.getPlan().equalsIgnoreCase("YEARLY")
                    ? yearlyPriceId : monthlyPriceId;

            // Create Stripe subscription
            com.stripe.model.Subscription stripeSubscription =
                    com.stripe.model.Subscription.create(
                            SubscriptionCreateParams.builder()
                                    .setCustomer(customer.getId())
                                    .addItem(SubscriptionCreateParams.Item.builder()
                                            .setPrice(priceId)
                                            .build())
                                    .setPaymentBehavior(
                                            SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                                    .addExpand("latest_invoice.payment_intent")
                                    .build()
                    );


            BigDecimal amount = request.getPlan().equalsIgnoreCase("YEARLY")
                    ? yearlyAmount : monthlyAmount;


            LocalDateTime renewalDate = request.getPlan().equalsIgnoreCase("YEARLY")
                    ? LocalDateTime.now().plusYears(1)
                    : LocalDateTime.now().plusMonths(1);


            Subscription subscription = Subscription.builder()
                    .userId(user.getId())
                    .plan(request.getPlan().toUpperCase())
                    .status("ACTIVE")
                    .stripeSubscriptionId(stripeSubscription.getId())
                    .stripeCustomerId(customer.getId())
                    .amount(amount)
                    .startDate(LocalDateTime.now())
                    .renewalDate(renewalDate)
                    .build();

            subscriptionRepository.save(subscription);

            // Record charity contribution
            recordCharityContribution(user, amount, subscription.getId());

            // Return client secret for frontend to confirm payment
            String clientSecret = stripeSubscription
                    .getLatestInvoiceObject()
                    .getPaymentIntentObject()
                    .getClientSecret();

            return CheckoutResponse.builder()
                    .clientSecret(clientSecret)
                    .subscriptionId(stripeSubscription.getId())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    @Transactional
    public void cancelSubscription(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Subscription subscription = subscriptionRepository
                .findByUserIdAndStatus(user.getId(), "ACTIVE")
                .orElseThrow(() -> new RuntimeException("No active subscription found"));

        try {
            com.stripe.model.Subscription stripeSubscription =
                    com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId());
            stripeSubscription.cancel();
        } catch (Exception e) {
            throw new RuntimeException("Stripe cancellation error: " + e.getMessage());
        }

        subscription.setStatus("CANCELLED");
        subscription.setEndDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    public SubscriptionResponse getMySubscription(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return subscriptionRepository
                .findByUserIdAndStatus(user.getId(), "ACTIVE")
                .map(this::mapToResponse)
                .orElse(null);
    }

    public List<SubscriptionResponse> getMySubscriptionHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return subscriptionRepository.findByUserId(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }


    @Transactional
    public void handleWebhookPaymentSuccess(String stripeSubscriptionId) {
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .ifPresent(sub -> {
                    sub.setStatus("ACTIVE");
                    sub.setRenewalDate(sub.getPlan().equals("YEARLY")
                            ? LocalDateTime.now().plusYears(1)
                            : LocalDateTime.now().plusMonths(1));
                    subscriptionRepository.save(sub);
                });
    }


    @Transactional
    public void handleWebhookPaymentFailed(String stripeSubscriptionId) {
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .ifPresent(sub -> {
                    sub.setStatus("LAPSED");
                    subscriptionRepository.save(sub);
                });
    }

    private void recordCharityContribution(User user, BigDecimal subscriptionAmount, UUID subscriptionId) {
        if (user.getCharityId() == null) return;

        BigDecimal percentage = BigDecimal.valueOf(user.getCharityPercentage())
                .divide(BigDecimal.valueOf(100));
        BigDecimal contribution = subscriptionAmount.multiply(percentage);

        CharityContribution charityContribution = CharityContribution.builder()
                .userId(user.getId())
                .charityId(user.getCharityId())
                .amount(contribution)
                .subscriptionId(subscriptionId)
                .build();

        contributionRepository.save(charityContribution);
    }

    private SubscriptionResponse mapToResponse(Subscription sub) {
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .plan(sub.getPlan())
                .status(sub.getStatus())
                .amount(sub.getAmount())
                .startDate(sub.getStartDate())
                .renewalDate(sub.getRenewalDate())
                .endDate(sub.getEndDate())
                .stripeSubscriptionId(sub.getStripeSubscriptionId())
                .build();
    }
}