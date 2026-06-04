package com.example.potatochip.ai.repository;

import com.example.potatochip.ai.entity.ProductRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, Long> {

    List<ProductRecommendation> findByUserIdAndExpiresAtAfterOrderByRankOrderAsc(Long userId, LocalDateTime now);

    List<ProductRecommendation> findBySessionIdAndExpiresAtAfterOrderByRankOrderAsc(String sessionId, LocalDateTime now);

    void deleteByUserId(Long userId);

    void deleteBySessionId(String sessionId);
}