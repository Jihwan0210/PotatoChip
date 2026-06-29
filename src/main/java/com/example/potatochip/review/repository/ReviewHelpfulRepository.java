package com.example.potatochip.review.repository;

import com.example.potatochip.review.entity.ReviewHelpful;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewHelpfulRepository extends JpaRepository<ReviewHelpful, Long> {

    @Query("""
            select count(rh) > 0
            from ReviewHelpful rh
            where rh.review.reviewId = :reviewId
              and rh.userId = :userId
            """)
    boolean existsByReviewIdAndUserId(
            @Param("reviewId") Long reviewId,
            @Param("userId") Long userId
    );

    @Query("""
            select count(rh)
            from ReviewHelpful rh
            where rh.review.reviewId = :reviewId
            """)
    Long countByReviewId(@Param("reviewId") Long reviewId);

    @Query("""
        select rh
        from ReviewHelpful rh
        where rh.review.reviewId = :reviewId
          and rh.userId = :userId
        """)
    Optional<ReviewHelpful> findByReviewIdAndUserId(
            @Param("reviewId") Long reviewId,
            @Param("userId") Long userId
    );

    @Modifying
    @Query("DELETE FROM ReviewHelpful rh WHERE rh.review.reviewId IN (SELECT r.reviewId FROM Review r WHERE r.product.id = :productId)")
    void deleteByProductId(@Param("productId") Long productId);
}
