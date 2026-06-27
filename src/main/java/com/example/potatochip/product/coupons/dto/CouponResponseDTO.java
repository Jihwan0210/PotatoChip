package com.example.potatochip.product.coupons.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Integer maxUsage;
    private Integer currentUsage;
    private Long productId;
    private String targetProductName;
    private LocalDateTime createdAt;
}