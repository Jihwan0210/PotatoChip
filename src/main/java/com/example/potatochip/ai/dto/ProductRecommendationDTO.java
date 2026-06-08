package com.example.potatochip.ai.dto;

import com.example.potatochip.ai.entity.ProductRecommendation;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductRecommendationDTO {

    private Long id;
    private Long userId;
    private String sessionId;
    private Long productId;
    private String productName;
    private String productCategory;
    private String thumbnailUrl;
    private String reason;
    private BigDecimal score;
    private Integer rankOrder;
    private LocalDateTime expiresAt;
    private LocalDateTime generatedAt;

    public static ProductRecommendationDTO fromEntity(ProductRecommendation recommendation) {
        return new ProductRecommendationDTO(
                recommendation.getId(),
                recommendation.getUserId(),
                recommendation.getSessionId(),
                recommendation.getProduct().getId(),
                recommendation.getProduct().getName(),
                recommendation.getProduct().getCategory(),
                recommendation.getProduct().getThumbnailUrl(),
                recommendation.getReason(),
                recommendation.getScore(),
                recommendation.getRankOrder(),
                recommendation.getExpiresAt(),
                recommendation.getGeneratedAt()
        );
    }
}