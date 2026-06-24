package com.example.golf.service;

        import com.example.golf.dto.reponse.*;
        import com.example.golf.dto.request.ScoreRequest;
        import com.example.golf.entity.User;
        import com.example.golf.repository.*;
        import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

        import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ScoreRepository scoreRepository;
    private final CharityRepository charityRepository;
    private final CharityContributionRepository contributionRepository;
    private final DrawRepository drawRepository;
    private final WinnerRepository winnerRepository;
    private final ScoreService scoreService;

    // ─── Stats ────────────────────────────────────────────────────────────
    public AdminStatsResponse getStats() {
        BigDecimal totalPool = subscriptionRepository.sumActiveSubscriptionAmounts();
        BigDecimal totalContributions = contributionRepository
                .findAll().stream()
                .map(c -> c.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .activeSubscribers(subscriptionRepository.countActiveSubscriptions())
                .totalPrizePool(totalPool != null ? totalPool : BigDecimal.ZERO)
                .totalCharityContributions(totalContributions)
                .totalDrawsPublished(
                        drawRepository.findByStatusOrderByDrawYearDescDrawMonthDesc("PUBLISHED").size())
                .pendingVerifications(winnerRepository.countPendingVerifications())
                .totalWinners(winnerRepository.count())
                .build();
    }


    public List<UserDetailResponse> getAllUsers() {
        return userRepository.findAll()
                .stream().map(this::mapToUserDetail).collect(Collectors.toList());
    }

    public UserDetailResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserDetail(user);
    }

    @Transactional
    public UserDetailResponse updateUserRole(UUID userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role.toUpperCase());
        return mapToUserDetail(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    // ─── Subscription Management ──────────────────────────────────────────
    @Transactional
    public void forceExpireSubscription(UUID userId) {
        subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .ifPresent(sub -> {
                    sub.setStatus("LAPSED");
                    subscriptionRepository.save(sub);
                });
    }

    // ─── Score Management (Admin can edit any user's scores) ──────────────
    public List<ScoreResponse> getUserScores(UUID userId) {
        return scoreService.getScoresByUserId(userId);
    }

    @Transactional
    public ScoreResponse addScoreForUser(UUID userId, ScoreRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Reuse score service logic via email lookup
        return scoreService.addScore(user.getEmail(), request);
    }

    @Transactional
    public ScoreResponse updateScoreForUser(UUID userId, UUID scoreId,
                                            ScoreRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return scoreService.updateScore(user.getEmail(), scoreId, request);
    }

    @Transactional
    public void deleteScoreForUser(UUID userId, UUID scoreId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        scoreService.deleteScore(user.getEmail(), scoreId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────
    private UserDetailResponse mapToUserDetail(User user) {
        // Active subscription
        SubscriptionResponse activeSub = subscriptionRepository
                .findByUserIdAndStatus(user.getId(), "ACTIVE")
                .map(sub -> SubscriptionResponse.builder()
                        .id(sub.getId())
                        .plan(sub.getPlan())
                        .status(sub.getStatus())
                        .amount(sub.getAmount())
                        .startDate(sub.getStartDate())
                        .renewalDate(sub.getRenewalDate())
                        .endDate(sub.getEndDate())
                        .build())
                .orElse(null);

        // Scores
        List<ScoreResponse> scores = scoreService.getScoresByUserId(user.getId());

        // Charity name
        String charityName = null;
        if (user.getCharityId() != null) {
            charityName = charityRepository.findById(user.getCharityId())
                    .map(c -> c.getName()).orElse(null);
        }

        // Total donated
        BigDecimal totalDonated = contributionRepository
                .sumAmountByUserId(user.getId());

        // Winnings
        List<WinnerResponse> winnings = winnerRepository
                .findByUserId(user.getId()).stream()
                .map(w -> WinnerResponse.builder()
                        .id(w.getId())
                        .drawId(w.getDrawId())
                        .matchType(w.getMatchType())
                        .prizeAmount(w.getPrizeAmount())
                        .verificationStatus(w.getVerificationStatus())
                        .payoutStatus(w.getPayoutStatus())
                        .build())
                .collect(Collectors.toList());

        return UserDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .charityId(user.getCharityId())
                .charityName(charityName)
                .charityPercentage(user.getCharityPercentage())
                .createdAt(user.getCreatedAt())
                .activeSubscription(activeSub)
                .scores(scores)
                .totalDonated(totalDonated != null ? totalDonated : BigDecimal.ZERO)
                .winnings(winnings)
                .build();
    }
}