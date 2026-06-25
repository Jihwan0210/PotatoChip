package com.example.potatochip.product.coupons.repository;

import com.example.potatochip.product.coupons.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);
    List<Coupon> findByCreatedByIdAndCreatedByRole(Long createdById, String role);
    java.util.Optional<Coupon> findFirstByProductIdAndIsActiveTrueOrderByCreatedAtDesc(Long productId);
    java.util.Optional<Coupon> findFirstByProductIdAndIsActiveTrueAndEndDateAfterOrderByCreatedAtDesc(Long productId, java.time.LocalDateTime now);
    java.util.Optional<Coupon> findFirstByProductIdAndCreatedByIdOrderByCreatedAtDesc(Long productId, Long sellerId);
}