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
    private Boolean isAnonymous;
    private String displayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long helpfulCount;
    private Boolean helpfulByCurrentUser;

    public static ReviewDTO fromEntity(Review review, Long helpfulCount, Boolean helpfulByCurrentUser) {
        Boolean isAnonymous = Boolean.TRUE.equals(review.getIsAnonymous());
        String displayName = isAnonymous ? "익명 구매자" : "구매자 " + review.getUserId();

        return new ReviewDTO(
                review.getReviewId(),
                review.getProductId(),
                review.getUserId(),
                review.getOrderItemId(),
                review.getRating(),
                review.getContent(),
                review.getImageUrl(),
                review.getRepurchaseIntent(),
                review.getIsAnonymous(),
                displayName,
                review.getCreatedAt(),
                review.getUpdatedAt(),
                helpfulCount,
                helpfulByCurrentUser
        );
    }
}