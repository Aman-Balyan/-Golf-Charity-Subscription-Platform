package service;


        import dto.reponse.DrawResponse;
        import dto.reponse.SimulationResponse;
        import dto.reponse.WinnerResponse;
        import dto.request.DrawRequest;
        import entity.Draw;
        import entity.Score;
        import entity.User;
        import entity.Winner;
        import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
        import repository.*;

        import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
        import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrawService {

    // Prize pool distribution percentages per PRD
    private static final BigDecimal FIVE_MATCH_SHARE  = new BigDecimal("0.40");
    private static final BigDecimal FOUR_MATCH_SHARE  = new BigDecimal("0.35");
    private static final BigDecimal THREE_MATCH_SHARE = new BigDecimal("0.25");

    private final DrawRepository drawRepository;
    private final WinnerRepository winnerRepository;
    private final UserRepository userRepository;
    private final ScoreRepository scoreRepository;
    private final SubscriptionRepository subscriptionRepository;

    // ─── Admin: create a new draw for a given month ───────────────────────
    @Transactional
    public DrawResponse createDraw(DrawRequest request) {
        // Prevent duplicate draw for same month/year
        drawRepository.findByDrawMonthAndDrawYear(
                        request.getDrawMonth(), request.getDrawYear())
                .ifPresent(d -> { throw new RuntimeException(
                        "Draw already exists for this month"); });

        Draw draw = Draw.builder()
                .drawMonth(request.getDrawMonth())
                .drawYear(request.getDrawYear())
                .drawType(request.getDrawType())
                .status("PENDING")
                .totalPool(calculateTotalPool())
                .build();

        return mapToResponse(drawRepository.save(draw), List.of());
    }

    // ─── Admin: simulate draw (preview without publishing) ───────────────
    public SimulationResponse simulate(UUID drawId) {
        Draw draw = getDraw(drawId);
        int[] winningNumbers = generateWinningNumbers(draw.getDrawType());
        BigDecimal totalPool = draw.getTotalPool();

        return buildSimulation(winningNumbers, totalPool);
    }

    // ─── Admin: run & publish the draw ───────────────────────────────────
    @Transactional
    public DrawResponse runAndPublish(UUID drawId) {
        Draw draw = getDraw(drawId);

        if ("PUBLISHED".equals(draw.getStatus())) {
            throw new RuntimeException("Draw already published");
        }

        int[] winningNumbers = generateWinningNumbers(draw.getDrawType());
        BigDecimal totalPool = draw.getTotalPool();

        // Handle jackpot rollover from previous month
        BigDecimal rolledOverJackpot = getPreviousRolledJackpot(
                draw.getDrawMonth(), draw.getDrawYear());
        BigDecimal jackpotPool = totalPool.multiply(FIVE_MATCH_SHARE)
                .add(rolledOverJackpot);
        BigDecimal fourMatchPool  = totalPool.multiply(FOUR_MATCH_SHARE);
        BigDecimal threeMatchPool = totalPool.multiply(THREE_MATCH_SHARE);

        // Find all active subscribers with scores
        List<User> activeSubscribers = getActiveSubscribers();
        Map<String, List<UUID>> matchGroups = new HashMap<>();
        matchGroups.put("FIVE_MATCH",  new ArrayList<>());
        matchGroups.put("FOUR_MATCH",  new ArrayList<>());
        matchGroups.put("THREE_MATCH", new ArrayList<>());

        for (User user : activeSubscribers) {
            List<Score> scores = scoreRepository
                    .findByUserIdOrderByPlayedDateDesc(user.getId());
            if (scores.isEmpty()) continue;

            // Use only the 5 most recent score values for matching
            List<Integer> userScores = scores.stream()
                    .limit(5)
                    .map(Score::getScore)
                    .collect(Collectors.toList());

            int matches = countMatches(winningNumbers, userScores);

            if (matches == 5) matchGroups.get("FIVE_MATCH").add(user.getId());
            else if (matches == 4) matchGroups.get("FOUR_MATCH").add(user.getId());
            else if (matches == 3) matchGroups.get("THREE_MATCH").add(user.getId());
        }

        // Handle jackpot rollover if no 5-match winner
        boolean jackpotWon = !matchGroups.get("FIVE_MATCH").isEmpty();
        BigDecimal newJackpot = jackpotWon ? BigDecimal.ZERO : jackpotPool;

        // Calculate per-winner prize amounts
        List<Winner> winners = new ArrayList<>();

        winners.addAll(createWinners(drawId,
                matchGroups.get("FIVE_MATCH"),
                "FIVE_MATCH",
                jackpotWon ? splitPrize(jackpotPool,
                        matchGroups.get("FIVE_MATCH").size()) : BigDecimal.ZERO));

        winners.addAll(createWinners(drawId,
                matchGroups.get("FOUR_MATCH"),
                "FOUR_MATCH",
                splitPrize(fourMatchPool, matchGroups.get("FOUR_MATCH").size())));

        winners.addAll(createWinners(drawId,
                matchGroups.get("THREE_MATCH"),
                "THREE_MATCH",
                splitPrize(threeMatchPool, matchGroups.get("THREE_MATCH").size())));

        winnerRepository.saveAll(winners);

        // Update draw
        draw.setWinningNumbers(winningNumbers);
        draw.setStatus("PUBLISHED");
        draw.setPublishedAt(LocalDateTime.now());
        draw.setJackpotAmount(newJackpot);
        draw.setTotalPool(totalPool);
        drawRepository.save(draw);

        List<WinnerResponse> winnerResponses = winners.stream()
                .map(this::mapWinnerToResponse)
                .collect(Collectors.toList());

        return mapToResponse(draw, winnerResponses);
    }

    // ─── Public: get all published draws ─────────────────────────────────
    public List<DrawResponse> getPublishedDraws() {
        return drawRepository.findByStatusOrderByDrawYearDescDrawMonthDesc("PUBLISHED")
                .stream()
                .map(d -> mapToResponse(d,
                        winnerRepository.findByDrawId(d.getId())
                                .stream().map(this::mapWinnerToResponse)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    // ─── Public: get single draw ──────────────────────────────────────────
    public DrawResponse getDrawById(UUID drawId) {
        Draw draw = getDraw(drawId);
        List<WinnerResponse> winners = winnerRepository.findByDrawId(drawId)
                .stream().map(this::mapWinnerToResponse).collect(Collectors.toList());
        return mapToResponse(draw, winners);
    }

    // ─── Admin: get all draws ─────────────────────────────────────────────
    public List<DrawResponse> getAllDraws() {
        return drawRepository.findAllByOrderByDrawYearDescDrawMonthDesc()
                .stream()
                .map(d -> mapToResponse(d,
                        winnerRepository.findByDrawId(d.getId())
                                .stream().map(this::mapWinnerToResponse)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    // ─── User: get my winnings ────────────────────────────────────────────
    public List<WinnerResponse> getMyWinnings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return winnerRepository.findByUserId(user.getId())
                .stream().map(this::mapWinnerToResponse).collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private int[] generateWinningNumbers(String drawType) {
        if ("ALGORITHMIC".equalsIgnoreCase(drawType)) {
            return generateAlgorithmic();
        }
        return generateRandom();
    }

    // Random: 5 unique numbers between 1–45
    private int[] generateRandom() {
        List<Integer> pool = new ArrayList<>();
        for (int i = 1; i <= 45; i++) pool.add(i);
        Collections.shuffle(pool);
        return pool.subList(0, 5).stream()
                .mapToInt(Integer::intValue).toArray();
    }

    // Algorithmic: weighted toward most common scores across all users
    private int[] generateAlgorithmic() {
        List<Score> allScores = scoreRepository.findAll();
        Map<Integer, Long> frequency = allScores.stream()
                .collect(Collectors.groupingBy(Score::getScore, Collectors.counting()));

        // Build weighted pool — higher frequency = more entries
        List<Integer> weightedPool = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : frequency.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                weightedPool.add(entry.getKey());
            }
        }

        // Fill remaining numbers if pool too small
        for (int i = 1; i <= 45; i++) {
            if (!weightedPool.contains(i)) weightedPool.add(i);
        }

        Collections.shuffle(weightedPool);

        // Pick 5 unique numbers
        Set<Integer> selected = new LinkedHashSet<>();
        for (int num : weightedPool) {
            selected.add(num);
            if (selected.size() == 5) break;
        }

        return selected.stream().mapToInt(Integer::intValue).toArray();
    }

    private int countMatches(int[] winningNumbers, List<Integer> userScores) {
        Set<Integer> winSet = new HashSet<>();
        for (int n : winningNumbers) winSet.add(n);
        int count = 0;
        for (int score : userScores) {
            if (winSet.contains(score)) count++;
        }
        return count;
    }

    private BigDecimal calculateTotalPool() {
        // Sum of a fixed % of all active subscriptions (adjust as needed)
        BigDecimal total = subscriptionRepository.sumActiveSubscriptionAmounts();
        return total != null ? total.multiply(new BigDecimal("0.50")) : BigDecimal.ZERO;
    }

    private BigDecimal getPreviousRolledJackpot(int month, int year) {
        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear  = month == 1 ? year - 1 : year;
        return drawRepository.findByDrawMonthAndDrawYear(prevMonth, prevYear)
                .map(Draw::getJackpotAmount)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal splitPrize(BigDecimal pool, int winnerCount) {
        if (winnerCount == 0) return BigDecimal.ZERO;
        return pool.divide(BigDecimal.valueOf(winnerCount), 2, RoundingMode.HALF_UP);
    }

    private List<User> getActiveSubscribers() {
        return userRepository.findAll().stream()
                .filter(u -> subscriptionRepository
                        .findByUserIdAndStatus(u.getId(), "ACTIVE").isPresent())
                .collect(Collectors.toList());
    }

    private List<Winner> createWinners(UUID drawId, List<UUID> userIds,
                                       String matchType, BigDecimal prize) {
        if (prize.compareTo(BigDecimal.ZERO) == 0) return List.of();
        return userIds.stream()
                .map(userId -> Winner.builder()
                        .drawId(drawId)
                        .userId(userId)
                        .matchType(matchType)
                        .prizeAmount(prize)
                        .verificationStatus("PENDING")
                        .payoutStatus("PENDING")
                        .build())
                .collect(Collectors.toList());
    }

    private SimulationResponse buildSimulation(int[] winningNumbers, BigDecimal totalPool) {
        List<User> activeSubscribers = getActiveSubscribers();

        int threeCount = 0, fourCount = 0, fiveCount = 0;
        List<WinnerResponse> projected = new ArrayList<>();

        for (User user : activeSubscribers) {
            List<Integer> scores = scoreRepository
                    .findByUserIdOrderByPlayedDateDesc(user.getId())
                    .stream().limit(5).map(Score::getScore).collect(Collectors.toList());
            if (scores.isEmpty()) continue;

            int matches = countMatches(winningNumbers, scores);
            String matchType = null;
            if      (matches == 5) { fiveCount++;  matchType = "FIVE_MATCH"; }
            else if (matches == 4) { fourCount++;  matchType = "FOUR_MATCH"; }
            else if (matches == 3) { threeCount++; matchType = "THREE_MATCH"; }

            if (matchType != null) {
                projected.add(WinnerResponse.builder()
                        .userId(user.getId())
                        .userFullName(user.getFullName())
                        .matchType(matchType)
                        .build());
            }
        }

        BigDecimal jackpotPool    = totalPool.multiply(FIVE_MATCH_SHARE);
        BigDecimal fourMatchPool  = totalPool.multiply(FOUR_MATCH_SHARE);
        BigDecimal threeMatchPool = totalPool.multiply(THREE_MATCH_SHARE);

        return SimulationResponse.builder()
                .winningNumbers(winningNumbers)
                .fiveMatchCount(fiveCount)
                .fourMatchCount(fourCount)
                .threeMatchCount(threeCount)
                .totalPool(totalPool)
                .jackpotPool(jackpotPool)
                .fourMatchPool(fourMatchPool)
                .threeMatchPool(threeMatchPool)
                .jackpotPerWinner(splitPrize(jackpotPool, fiveCount))
                .fourMatchPerWinner(splitPrize(fourMatchPool, fourCount))
                .threeMatchPerWinner(splitPrize(threeMatchPool, threeCount))
                .projectedWinners(projected)
                .build();
    }

    private Draw getDraw(UUID drawId) {
        return drawRepository.findById(drawId)
                .orElseThrow(() -> new RuntimeException("Draw not found"));
    }

    private DrawResponse mapToResponse(Draw draw, List<WinnerResponse> winners) {
        return DrawResponse.builder()
                .id(draw.getId())
                .drawMonth(draw.getDrawMonth())
                .drawYear(draw.getDrawYear())
                .drawType(draw.getDrawType())
                .status(draw.getStatus())
                .winningNumbers(draw.getWinningNumbers())
                .jackpotAmount(draw.getJackpotAmount())
                .totalPool(draw.getTotalPool())
                .publishedAt(draw.getPublishedAt())
                .winners(winners)
                .build();
    }

    private WinnerResponse mapWinnerToResponse(Winner winner) {
        String fullName = userRepository.findById(winner.getUserId())
                .map(User::getFullName).orElse("Unknown");
        return WinnerResponse.builder()
                .id(winner.getId())
                .drawId(winner.getDrawId())
                .userId(winner.getUserId())
                .userFullName(fullName)
                .matchType(winner.getMatchType())
                .prizeAmount(winner.getPrizeAmount())
                .verificationStatus(winner.getVerificationStatus())
                .payoutStatus(winner.getPayoutStatus())
                .proofUrl(winner.getProofUrl())
                .createdAt(winner.getCreatedAt())
                .build();
    }
}