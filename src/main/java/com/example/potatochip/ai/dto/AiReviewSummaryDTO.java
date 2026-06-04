package com.example.potatochip.ai.dto;

import com.example.potatochip.ai.entity.AiReviewSummary;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AiReviewSummaryDTO {

    private Long id;
    private Long productId;
    private String summary;
    private Integer reviewCount;
    private String status;
    private Boolean isActive;
    private String errorMessage;
    private LocalDateTime generatedAt;
    private LocalDateTime updatedAt;

    public static AiReviewSummaryDTO fromEntity(AiReviewSummary aiReviewSummary) {
        return new AiReviewSummaryDTO(
                aiReviewSummary.getId(),
                aiReviewSummary.getProductId(),
                aiReviewSummary.getSummary(),
                aiReviewSummary.getReviewCount(),
                aiReviewSummary.getStatus(),
                aiReviewSummary.getIsActive(),
                aiReviewSummary.getErrorMessage(),
                aiReviewSummary.getGeneratedAt(),
                aiReviewSummary.getUpdatedAt()
        );
    }
}