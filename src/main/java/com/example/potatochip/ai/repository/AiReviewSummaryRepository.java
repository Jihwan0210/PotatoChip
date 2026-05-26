package com.example.potatochip.ai.repository;

import com.example.potatochip.ai.entity.AiReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiReviewSummaryRepository extends JpaRepository<AiReviewSummary, Long> {

    Optional<AiReviewSummary> findByProductIdAndIsActiveTrue(Long productId);

    boolean existsByProductIdAndIsActiveTrue(Long productId);

    // TODO: Product Entity 연관관계 적용 후 메서드명 수정 여부 확인 필요
    //
    // 현재:
    // Optional<AiReviewSummary> findByProductIdAndIsActiveTrue(Long productId);
    // boolean existsByProductIdAndIsActiveTrue(Long productId);
    //
    // 변경 가능:
    // Optional<AiReviewSummary> findByProductAndIsActiveTrue(Product product);
    // boolean existsByProductAndIsActiveTrue(Product product);
}