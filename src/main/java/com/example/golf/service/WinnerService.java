package com.example.golf.service;


import com.example.golf.dto.reponse.WinnerResponse;
import com.example.golf.dto.request.ProofUploadRequest;
import com.example.golf.dto.request.VerificationRequest;
import com.example.golf.entity.User;
import com.example.golf.entity.Winner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.golf.repository.UserRepository;
import com.example.golf.repository.WinnerRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WinnerService {

    private final WinnerRepository winnerRepository;
    private final UserRepository userRepository;

    // User: upload proof screenshot URL
    @Transactional
    public WinnerResponse uploadProof(String email, UUID winnerId,
                                      ProofUploadRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Winner winner = winnerRepository.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("Winner record not found"));

        if (!winner.getUserId().equals(user.getId())) {
            throw new RuntimeException("Not authorised");
        }

        if (!"PENDING".equals(winner.getVerificationStatus())) {
            throw new RuntimeException("Proof already submitted");
        }

        winner.setProofUrl(request.getProofUrl());
        return mapToResponse(winnerRepository.save(winner));
    }

    // Admin: get all winners pending verification
    public List<WinnerResponse> getPendingVerifications() {
        return winnerRepository.findAll().stream()
                .filter(w -> "PENDING".equals(w.getVerificationStatus())
                        && w.getProofUrl() != null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Admin: approve or reject a winner
    @Transactional
    public WinnerResponse verifyWinner(UUID winnerId, VerificationRequest request) {
        Winner winner = winnerRepository.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("Winner not found"));

        String status = request.getStatus().toUpperCase();
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            throw new RuntimeException("Status must be APPROVED or REJECTED");
        }

        winner.setVerificationStatus(status);
        return mapToResponse(winnerRepository.save(winner));
    }

    // Admin: mark payout as completed
    @Transactional
    public WinnerResponse markPaid(UUID winnerId) {
        Winner winner = winnerRepository.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("Winner not found"));

        if (!"APPROVED".equals(winner.getVerificationStatus())) {
            throw new RuntimeException("Winner must be approved before marking as paid");
        }

        winner.setPayoutStatus("PAID");
        return mapToResponse(winnerRepository.save(winner));
    }

    // Admin: all winners for a specific draw
    public List<WinnerResponse> getWinnersByDraw(UUID drawId) {
        return winnerRepository.findByDrawId(drawId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Admin: full winners list
    public List<WinnerResponse> getAllWinners() {
        return winnerRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private WinnerResponse mapToResponse(Winner winner) {
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