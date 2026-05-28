package com.example.potatochip.review.dto;

import com.example.potatochip.review.entity.Review;
import java.time.LocalDateTime;


public class ReviewResponse {

    private Long reviewId;
    private Long productId;
    private Long userId;
    private Long orderItemId;
    private Integer rating;
    private String content;
    private String imageUrl;
    private Boolean repurchaseIntent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewResponse(
            Long reviewId,
            Long productId,
            Long userId,
            Long orderItemId,
            Integer rating,
            String content,
            String imageUrl,
            Boolean repurchaseIntent,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.userId = userId;
        this.orderItemId = orderItemId;
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;
        this.repurchaseIntent = repurchaseIntent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReviewResponse fromEntity(Review review) {
        return new ReviewResponse(
                review.getReviewId(),
                review.getProductId(),
                review.getUserId(),
                review.getOrderItemId(),
                review.getRating(),
                review.getContent(),
                review.getImageUrl(),
                review.getRepurchaseIntent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}