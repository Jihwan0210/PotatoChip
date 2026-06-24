package com.example.potatochip.product.coupons;

import com.example.potatochip.product.coupons.entity.Coupon;
import com.example.potatochip.product.coupons.entity.DiscountType;
import com.example.potatochip.product.coupons.repository.CouponRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WelcomeCouponInitializer {

    private static final String WELCOME_CODE = "WELCOME10";

    private final CouponRepository couponRepository;

    @PostConstruct
    public void init() {
        if (couponRepository.existsByCode(WELCOME_CODE)) return;

        couponRepository.save(Coupon.builder()
                .code(WELCOME_CODE)
                .name("신규 회원 가입 10% 할인")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .minOrderAmount(null)
                .maxDiscountAmount(BigDecimal.valueOf(5000))  // 최대 5,000원까지
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.of(2099, 12, 31, 23, 59, 59))
                .isActive(true)
                .maxUsage(null)
                .currentUsage(0)
                .createdById(0L)   // 시스템 발급
                .createdByRole("ADMIN")
                .build());
    }

    public static String getWelcomeCode() {
        return WELCOME_CODE;
    }
}
