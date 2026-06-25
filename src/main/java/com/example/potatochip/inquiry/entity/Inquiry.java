package com.example.potatochip.inquiry.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 300)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "answered_by")
    private Long answeredBy;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder
    public Inquiry(Long userId, Long productId, Long orderId, Long orderItemId, String category, String title, String content, String imageUrl) {
        this.userId = userId;
        this.productId = productId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.status = "pending";
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isWrittenBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void updateInquiry(String category, String title, String content, Long productId, Long orderId, Long orderItemId, String imageUrl) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.productId = productId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void answer(String answer, Long answeredBy) {
        this.answer = answer;
        this.answeredBy = answeredBy;
        this.answeredAt = LocalDateTime.now();
        this.status = "answered";
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = "pending";
        if (this.isActive == null) this.isActive = true;
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
