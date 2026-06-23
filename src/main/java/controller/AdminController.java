package controller;


import dto.reponse.AdminStatsResponse;
import dto.reponse.ScoreResponse;
import dto.reponse.UserDetailResponse;
import dto.request.ScoreRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.AdminService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // Dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ─── User Management ──────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<List<UserDetailResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailResponse> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserDetailResponse> updateRole(
            @PathVariable UUID userId,
            @RequestParam String role) {
        return ResponseEntity.ok(adminService.updateUserRole(userId, role));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/expire-subscription")
    public ResponseEntity<Void> expireSubscription(@PathVariable UUID userId) {
        adminService.forceExpireSubscription(userId);
        return ResponseEntity.ok().build();
    }

    // ─── Score Management ─────────────────────────────────────────────────
    @GetMapping("/users/{userId}/scores")
    public ResponseEntity<List<ScoreResponse>> getUserScores(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getUserScores(userId));
    }

    @PostMapping("/users/{userId}/scores")
    public ResponseEntity<ScoreResponse> addScore(
            @PathVariable UUID userId,
            @Valid @RequestBody ScoreRequest request) {
        return ResponseEntity.ok(adminService.addScoreForUser(userId, request));
    }

    @PutMapping("/users/{userId}/scores/{scoreId}")
    public ResponseEntity<ScoreResponse> updateScore(
            @PathVariable UUID userId,
            @PathVariable UUID scoreId,
            @Valid @RequestBody ScoreRequest request) {
        return ResponseEntity.ok(adminService.updateScoreForUser(userId, scoreId, request));
    }

    @DeleteMapping("/users/{userId}/scores/{scoreId}")
    public ResponseEntity<Void> deleteScore(
            @PathVariable UUID userId,
            @PathVariable UUID scoreId) {
        adminService.deleteScoreForUser(userId, scoreId);
        return ResponseEntity.noContent().build();
    }
}