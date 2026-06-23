package controller;

import dto.reponse.DrawResponse;
import dto.reponse.SimulationResponse;
import dto.reponse.WinnerResponse;
import dto.request.DrawRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import service.DrawService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/draws")
@RequiredArgsConstructor
public class DrawController {

    private final DrawService drawService;

    // Public: all published draws
    @GetMapping("/published")
    public ResponseEntity<List<DrawResponse>> getPublished() {
        return ResponseEntity.ok(drawService.getPublishedDraws());
    }

    // Public: single draw
    @GetMapping("/{drawId}")
    public ResponseEntity<DrawResponse> getById(@PathVariable UUID drawId) {
        return ResponseEntity.ok(drawService.getDrawById(drawId));
    }

    // User: my winnings
    @GetMapping("/my-winnings")
    public ResponseEntity<List<WinnerResponse>> getMyWinnings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(drawService.getMyWinnings(userDetails.getUsername()));
    }

    // Admin: all draws
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DrawResponse>> getAll() {
        return ResponseEntity.ok(drawService.getAllDraws());
    }

    // Admin: create draw
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DrawResponse> create(@Valid @RequestBody DrawRequest request) {
        return ResponseEntity.ok(drawService.createDraw(request));
    }

    // Admin: simulate draw
    @GetMapping("/{drawId}/simulate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SimulationResponse> simulate(@PathVariable UUID drawId) {
        return ResponseEntity.ok(drawService.simulate(drawId));
    }

    // Admin: run and publish
    @PostMapping("/{drawId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DrawResponse> publish(@PathVariable UUID drawId) {
        return ResponseEntity.ok(drawService.runAndPublish(drawId));
    }
}