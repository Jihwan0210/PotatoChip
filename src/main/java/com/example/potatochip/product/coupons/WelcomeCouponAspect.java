package com.example.potatochip.product.coupons;

import com.example.potatochip.auth.dto.UserDTO;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.coupons.entity.Coupon;
import com.example.potatochip.product.coupons.entity.UserCoupon;
import com.example.potatochip.product.coupons.repository.CouponRepository;
import com.example.potatochip.product.coupons.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class WelcomeCouponAspect {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    @AfterReturning("execution(* com.example.potatochip.auth.service.AuthService.signup(..))")
    public void issueWelcomeCoupon(JoinPoint joinPoint) {
        try {
            UserDTO dto = (UserDTO) joinPoint.getArgs()[0];

            userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
                couponRepository.findByCode(WelcomeCouponInitializer.getWelcomeCode())
                        .ifPresent(coupon -> {
                            if (!userCouponRepository.existsByUserIdAndCouponId(user.getId(), coupon.getId())) {
                                userCouponRepository.save(UserCoupon.builder()
                                        .user(user)
                                        .coupon(coupon)
                                        .isUsed(false)
                                        .build());
                            }
                        });
            });
        } catch (Exception e) {
            log.warn("웰컴 쿠폰 지급 실패: {}", e.getMessage());
        }
    }
}