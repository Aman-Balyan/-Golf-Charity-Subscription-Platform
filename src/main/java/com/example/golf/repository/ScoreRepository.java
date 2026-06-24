package com.example.golf.repository;


import com.example.golf.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ScoreRepository extends JpaRepository<Score, UUID> {

    // Get all scores for user, newest first
    List<Score> findByUserIdOrderByPlayedDateDesc(UUID userId);

    // Count scores for user
    long countByUserId(UUID userId);

    // Get oldest score for user (to delete when > 5)
    @Query("SELECT s FROM Score s WHERE s.userId = :userId ORDER BY s.playedDate ASC, s.createdAt ASC")
    List<Score> findByUserIdOrderByPlayedDateAsc(@Param("userId") UUID userId);
}