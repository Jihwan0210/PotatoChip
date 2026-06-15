package com.example.potatochip.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Builder
@Entity
@Table(name = "order_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    private Long productId;
    private Long sellerId;
    private int quantity;
    private BigDecimal price;
    @Column(name = "shipping_fee", nullable = false)
    private BigDecimal shippingFee = BigDecimal.ZERO; // 기본 배송비 0원
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PAYMENT_COMPLETE; // 기본 상태: 결제완료
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
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
