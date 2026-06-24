package com.example.golf.repository;

import com.example.golf.entity.Winner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface WinnerRepository extends JpaRepository<Winner, UUID> {
    List<Winner> findByDrawId(UUID drawId);
    List<Winner> findByUserId(UUID userId);
    List<Winner> findByMatchType(String matchType);

    @Query("SELECT w FROM Winner w WHERE w.drawId = :drawId AND w.matchType = :matchType")
    List<Winner> findByDrawIdAndMatchType(
            @Param("drawId") UUID drawId,
            @Param("matchType") String matchType);

    @Query("SELECT COUNT(w) FROM Winner w WHERE w.verificationStatus = 'PENDING'")
    long countPendingVerifications();
}