package com.example.potatochip.product.coupons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CouponCreateDTO {
    private String code;
    private String name;
    private String discountType;       // PERCENTAGE / FIXED_AMOUNT / FREE_SHIPPING
    private BigDecimal discountValue;  // % 또는 원, FREE_SHIPPING이면 null
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxUsage;          // null = 무제한
    private Long productId;            // null = 전체 상품 적용, 값 있으면 해당 상품 전용
}