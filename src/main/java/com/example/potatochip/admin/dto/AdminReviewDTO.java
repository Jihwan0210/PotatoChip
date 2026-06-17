package com.example.potatochip.admin.dto;

import com.example.potatochip.product.entity.Product;
import com.example.potatochip.review.entity.Review;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewDTO {

    private Long reviewId;
    private Long productId;
    private String productName;
    private Long sellerId;
    private String sellerName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String content;
    private String imageUrl;
    private Boolean hasImage;
    private Boolean hidden;
    private Boolean repurchaseIntent;
    private Boolean anonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminReviewDTO fromEntity(Review review, String userName, String sellerName) {
        Product product = review.getProduct();

        return AdminReviewDTO.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProductId())
                .productName(product == null ? null : product.getName())
                .sellerId(product == null || product.getSeller() == null ? null : product.getSeller().getId())
                .sellerName(sellerName)
                .userId(review.getUserId())
                .userName(userName)
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrl(review.getImageUrl())
                .hasImage(review.getImageUrl() != null && !review.getImageUrl().isBlank())
                .hidden(Boolean.TRUE.equals(review.getIsHidden()))
                .repurchaseIntent(review.getRepurchaseIntent())
                .anonymous(review.getIsAnonymous())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}