package com.example.potatochip.review.repository;

import com.example.potatochip.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndIsActiveTrueOrderByCreatedAtDesc(Long productId);

    Optional<Review> findByReviewIdAndIsActiveTrue(Long reviewId);

    Long countByProductIdAndIsActiveTrue(Long productId);

    Long countByProductIdAndIsActiveTrueAndRepurchaseIntentTrue(Long productId);

    @Query("""
            select count(r)
            from Review r
            where r.productId = :productId
              and r.isActive = true
              and r.imageUrl is not null
              and trim(r.imageUrl) <> ''
            """)
    Long countPhotoReviewsByProductId(@Param("productId") Long productId);

    @Query("""
            select avg(r.rating)
            from Review r
            where r.productId = :productId
              and r.isActive = true
            """)
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // TODO: User Entity, Product Entity, OrderItem Entity가 main 브랜치에 병합되면
    //  Long productId, Long userId, Long orderItemId 기준 조회를
    //  연관관계 기반 조회로 변경할지 확인 필요
}