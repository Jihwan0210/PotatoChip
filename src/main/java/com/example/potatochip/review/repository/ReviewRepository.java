package com.example.potatochip.review.repository;

import com.example.potatochip.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
            select r
            from Review r
            where r.product.id = :productId
              and r.isActive = true
              and r.isHidden = false
            order by r.createdAt desc
            """)
    List<Review> findByProductIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("productId") Long productId);

    Optional<Review> findByReviewIdAndIsActiveTrue(Long reviewId);

    @Query("""
            select count(r)
            from Review r
            where r.product.id = :productId
              and r.isActive = true
              and r.isHidden = false
            """)
    Long countByProductIdAndIsActiveTrue(@Param("productId") Long productId);

    @Query("""
            select count(r)
            from Review r
            where r.product.id = :productId
              and r.isActive = true
              and r.isHidden = false
              and r.repurchaseIntent = true
            """)
    Long countByProductIdAndIsActiveTrueAndRepurchaseIntentTrue(@Param("productId") Long productId);

    @Query("""
            select count(r)
            from Review r
            where r.product.id = :productId
              and r.isActive = true
              and r.isHidden = false
              and r.imageUrl is not null
              and trim(r.imageUrl) <> ''
            """)
    Long countPhotoReviewsByProductId(@Param("productId") Long productId);

    @Query("""
            select avg(r.rating)
            from Review r
            where r.product.id = :productId
              and r.isActive = true
              and r.isHidden = false
            """)
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    List<Review> findByIsActiveTrueOrderByCreatedAtDesc();

    List<Review> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    long countByIsActiveTrue();

    @Query("""
            select r
            from Review r
            where r.product.seller.id = :sellerId
              and r.isActive = true
            order by r.createdAt desc
            """)
    List<Review> findBySellerIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("sellerId") Long sellerId);
}