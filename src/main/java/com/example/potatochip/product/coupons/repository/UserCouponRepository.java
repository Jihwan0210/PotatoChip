package com.example.potatochip.product.coupons.repository;

import com.example.potatochip.product.coupons.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon c " +
           "WHERE uc.user.id = :userId AND uc.isUsed = false " +
           "AND c.isActive = true AND c.startDate <= :now AND c.endDate >= :now")
    List<UserCoupon> findAvailableCoupons(@Param("userId") Long userId,
                                          @Param("now") LocalDateTime now);
}