package com.example.potatochip.review.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Review() {
    }

    public Review(
            Long productId,
            Long userId,
            Long orderItemId,
            Integer rating,
            String content,
            String imageUrl,
            Boolean repurchaseIntent
    ) {
        this.productId = productId;
        this.userId = userId;
        this.orderItemId = orderItemId;
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;
        this.repurchaseIntent = repurchaseIntent;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Boolean getRepurchaseIntent() {
        return repurchaseIntent;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isWrittenBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isBlank();
    }

    public void updateReview(Integer rating, String content, String imageUrl, Boolean repurchaseIntent) {
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;
        this.repurchaseIntent = repurchaseIntent;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}