package com.example.golf.controller;

import com.example.golf.dto.reponse.CharityResponse;
import com.example.golf.dto.request.CharityRequest;
import com.example.golf.dto.request.CharitySelectionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.golf.service.CharityService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/charities")
@RequiredArgsConstructor
public class CharityController {

    private final CharityService charityService;

    // Public endpoints
    @GetMapping
    public ResponseEntity<List<CharityResponse>> getAll() {
        return ResponseEntity.ok(charityService.getAllActive());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<CharityResponse>> getFeatured() {
        return ResponseEntity.ok(charityService.getFeatured());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CharityResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(charityService.search(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharityResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(charityService.getById(id));
    }

    // User: select charity
    @PostMapping("/select")
    public ResponseEntity<Void> selectCharity(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CharitySelectionRequest request) {
        charityService.selectCharity(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    // Admin endpoints
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CharityResponse> create(
            @Valid @RequestBody CharityRequest request) {
        return ResponseEntity.ok(charityService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CharityResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CharityRequest request) {
        return ResponseEntity.ok(charityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        charityService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}