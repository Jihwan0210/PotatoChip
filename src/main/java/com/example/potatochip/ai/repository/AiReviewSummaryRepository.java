package com.example.potatochip.ai.repository;

import com.example.potatochip.ai.entity.AiReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiReviewSummaryRepository extends JpaRepository<AiReviewSummary, Long> {

    @Query("""
            select a
            from AiReviewSummary a
            where a.product.id = :productId
              and a.isActive = true
            """)
    Optional<AiReviewSummary> findByProductIdAndIsActiveTrue(@Param("productId") Long productId);

    @Query("""
            select a
            from AiReviewSummary a
            where a.product.id = :productId
            """)
    Optional<AiReviewSummary> findByProductId(@Param("productId") Long productId);

    @Query("""
            select count(a) > 0
            from AiReviewSummary a
            where a.product.id = :productId
              and a.isActive = true
            """)
    boolean existsByProductIdAndIsActiveTrue(@Param("productId") Long productId);
}