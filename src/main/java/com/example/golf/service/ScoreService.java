package com.example.golf.service;

import com.example.golf.dto.reponse.ScoreResponse;
import com.example.golf.dto.request.ScoreRequest;
import com.example.golf.entity.Score;
import com.example.golf.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.golf.repository.ScoreRepository;
import com.example.golf.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private static final int MAX_SCORES = 5;

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    @Transactional
    public ScoreResponse addScore(String email, ScoreRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // If user already has 5 scores, delete the oldest one
        long count = scoreRepository.countByUserId(user.getId());
        if (count >= MAX_SCORES) {
            List<Score> oldest = scoreRepository
                    .findByUserIdOrderByPlayedDateAsc(user.getId());
            scoreRepository.delete(oldest.get(0));
        }

        Score score = Score.builder()
                .userId(user.getId())
                .score(request.getScore())
                .playedDate(request.getPlayedDate())
                .build();

        Score saved = scoreRepository.save(score);
        return mapToResponse(saved);
    }

    public List<ScoreResponse> getMyScores(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return scoreRepository
                .findByUserIdOrderByPlayedDateDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScoreResponse updateScore(String email, UUID scoreId, ScoreRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Score score = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Score not found"));

        // Make sure the score belongs to this user
        if (!score.getUserId().equals(user.getId())) {
            throw new RuntimeException("Not authorised to edit this score");
        }

        score.setScore(request.getScore());
        score.setPlayedDate(request.getPlayedDate());

        return mapToResponse(scoreRepository.save(score));
    }

    @Transactional
    public void deleteScore(String email, UUID scoreId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Score score = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Score not found"));

        if (!score.getUserId().equals(user.getId())) {
            throw new RuntimeException("Not authorised to delete this score");
        }

        scoreRepository.delete(score);
    }

    // Admin: get scores for any user
    public List<ScoreResponse> getScoresByUserId(UUID userId) {
        return scoreRepository
                .findByUserIdOrderByPlayedDateDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ScoreResponse mapToResponse(Score score) {
        return ScoreResponse.builder()
                .id(score.getId())
                .score(score.getScore())
                .playedDate(score.getPlayedDate())
                .createdAt(score.getCreatedAt())
                .build();
    }
}