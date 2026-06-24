package com.example.potatochip.product.coupons.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCouponDTO {
    private Long userCouponId;
    private Long couponId;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime endDate;
    private Long targetProductId;
    private String targetProductName;  // null이면 전체 상품 적용
}