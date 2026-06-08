package com.example.potatochip.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.A;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;
    private Long buyerId;
    private String shippingAddress;
    private BigDecimal totalAmount;
    @Column
    @Builder.Default
    private BigDecimal totalShippingFee = BigDecimal.ZERO;
    @Column
    private String paymentMethod;
    @Column
    @Builder.Default
    private String deliveryType = "delivery";
    @Column(name = "pickup_datetime")
    private LocalDateTime pickupDatetime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PAYMENT_COMPLETE;
    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    // 🌟 영수증 상세 내역(OrderItem)들을 통제하는 리스트
//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<OrderItem> orderItems = new ArrayList<>();
//    public void addOrderItem(OrderItem item) {
//        this.orderItems.add(item);
//        item.setOrder(this);
//    }
//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//    }
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }

}
