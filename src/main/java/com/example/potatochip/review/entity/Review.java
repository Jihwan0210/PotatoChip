package com.example.potatochip.review.entity;

import com.example.potatochip.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "repurchase_intent", nullable = false)
    private Boolean repurchaseIntent;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden;

    @Builder
    public Review(
            Product product,
            Long userId,
            Long orderItemId,
            Integer rating,
            String content,
            String imageUrl,
            Boolean repurchaseIntent,
            Boolean isAnonymous
    ) {
        this.product = product;
        this.userId = userId;
        this.orderItemId = orderItemId;
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;
        this.repurchaseIntent = Boolean.TRUE.equals(repurchaseIntent);
        this.isAnonymous = Boolean.TRUE.equals(isAnonymous);
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isHidden = false;
    }

    public Long getProductId() {
        return product.getId();
    }

    public boolean isWrittenBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void updateReview(
            Integer rating,
            String content,
            String imageUrl,
            Boolean repurchaseIntent,
            Boolean isAnonymous
    ) {
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;
        this.repurchaseIntent = Boolean.TRUE.equals(repurchaseIntent);
        this.isAnonymous = Boolean.TRUE.equals(isAnonymous);
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.repurchaseIntent == null) {
            this.repurchaseIntent = false;
        }

        if (this.isAnonymous == null) {
            this.isAnonymous = false;
        }

        if (this.isActive == null) {
            this.isActive = true;
        }

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isHidden == null) {
            this.isHidden = false;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void hide() {
        this.isHidden = Boolean.TRUE;
        this.updatedAt = LocalDateTime.now();
    }

    public void show() {
        this.isHidden = false;
        this.updatedAt = LocalDateTime.now();
    }
}
