package com.example.potatochip.product.coupons.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    // PERCENTAGE: 할인율(%), FIXED_AMOUNT: 할인금액(원), FREE_SHIPPING: null
    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;

    // PERCENTAGE 쿠폰의 최대 할인 한도
    private BigDecimal maxDiscountAmount;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // null = 무제한
    private Integer maxUsage;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentUsage = 0;

    // 특정 상품 전용 쿠폰 (null = 전체 상품 적용)
    private Long productId;

    @Column(nullable = false)
    private Long createdById;

    @Column(nullable = false, length = 10)
    private String createdByRole;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}