package com.example.potatochip.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tid", nullable = false)
    private String tid;          // 카카오 결제 고유번호

    @Column(name = "method")
    private String method;       // kakaopay / card / bank 등

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "status")
    private String status;       // READY / APPROVED / FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}