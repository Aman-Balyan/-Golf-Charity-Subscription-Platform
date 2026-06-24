package com.example.golf.repository;

import com.example.golf.entity.Draw;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DrawRepository extends JpaRepository<Draw, UUID> {
    Optional<Draw> findByDrawMonthAndDrawYear(Integer month, Integer year);
    List<Draw> findByStatusOrderByDrawYearDescDrawMonthDesc(String status);
    List<Draw> findAllByOrderByDrawYearDescDrawMonthDesc();
}