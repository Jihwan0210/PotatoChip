package com.example.potatochip.review.dto;

import com.example.potatochip.review.entity.Review;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewDTO {

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

    private Long helpfulCount;

    private Boolean helpfulByCurrentUser;

    public static ReviewDTO fromEntity(Review review) {
        return fromEntity(review, 0L, false);
    }

    public static ReviewDTO fromEntity(
            Review review,
            Long helpfulCount,
            Boolean helpfulByCurrentUser
    ) {
        return new ReviewDTO(
                review.getReviewId(),
                review.getProductId(),
                review.getUserId(),
                review.getOrderItemId(),
                review.getRating(),
                review.getContent(),
                review.getImageUrl(),
                review.getRepurchaseIntent(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                helpfulCount,
                helpfulByCurrentUser
        );
    }
}