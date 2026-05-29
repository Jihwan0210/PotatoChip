package com.example.potatochip.review.dto;

import com.example.potatochip.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
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
}