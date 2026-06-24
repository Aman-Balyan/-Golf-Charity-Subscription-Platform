package com.example.golf.controller;


import com.example.golf.dto.reponse.ScoreResponse;
import com.example.golf.dto.request.ScoreRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.golf.service.ScoreService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    // Add a new score
    @PostMapping
    public ResponseEntity<ScoreResponse> addScore(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ScoreRequest request) {
        return ResponseEntity.ok(
                scoreService.addScore(userDetails.getUsername(), request));
    }

    // Get my scores (newest first)
    @GetMapping
    public ResponseEntity<List<ScoreResponse>> getMyScores(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                scoreService.getMyScores(userDetails.getUsername()));
    }

    // Edit a score
    @PutMapping("/{scoreId}")
    public ResponseEntity<ScoreResponse> updateScore(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID scoreId,
            @Valid @RequestBody ScoreRequest request) {
        return ResponseEntity.ok(
                scoreService.updateScore(userDetails.getUsername(), scoreId, request));
    }

    // Delete a score
    @DeleteMapping("/{scoreId}")
    public ResponseEntity<Void> deleteScore(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID scoreId) {
        scoreService.deleteScore(userDetails.getUsername(), scoreId);
        return ResponseEntity.noContent().build();
    }
}