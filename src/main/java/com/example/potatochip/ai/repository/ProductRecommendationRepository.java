package com.example.potatochip.ai.repository;

import com.example.potatochip.ai.entity.ProductRecommendation;
import com.example.potatochip.ai.entity.ProductRecommendationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, Long> {

    List<ProductRecommendation> findByUserIdAndTypeAndExpiresAtAfterOrderByRankOrderAsc(
            Long userId,
            ProductRecommendationType type,
            LocalDateTime now
    );

    List<ProductRecommendation> findBySessionIdAndTypeAndExpiresAtAfterOrderByRankOrderAsc(
            String sessionId,
            ProductRecommendationType type,
            LocalDateTime now
    );

    List<ProductRecommendation> findByTypeAndExpiresAtAfterOrderByRankOrderAsc(
            ProductRecommendationType type,
            LocalDateTime now
    );

    void deleteByUserIdAndType(Long userId, ProductRecommendationType type);

    void deleteBySessionIdAndType(String sessionId, ProductRecommendationType type);

    void deleteByType(ProductRecommendationType type);

    @Modifying
    @Query("DELETE FROM ProductRecommendation pr WHERE pr.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
