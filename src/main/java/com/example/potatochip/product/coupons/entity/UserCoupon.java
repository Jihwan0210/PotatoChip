package com.example.potatochip.product.coupons.entity;

import com.example.potatochip.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "coupon_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    private LocalDateTime usedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime obtainedAt;

    @PrePersist
    protected void onCreate() {
        this.obtainedAt = LocalDateTime.now();
    }
}