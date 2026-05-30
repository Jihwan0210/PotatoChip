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

    private Long aiReviewSummaryId;

    private Long productId;

    private String summary;

    private Integer reviewCount;

    private Boolean isActive;

    private LocalDateTime generatedAt;

    private LocalDateTime updatedAt;

    public static AiReviewSummaryDTO fromEntity(AiReviewSummary aiReviewSummary) {
        return new AiReviewSummaryDTO(
                aiReviewSummary.getAiReviewSummaryId(),
                aiReviewSummary.getProductId(),
                aiReviewSummary.getSummary(),
                aiReviewSummary.getReviewCount(),
                aiReviewSummary.getIsActive(),
                aiReviewSummary.getGeneratedAt(),
                aiReviewSummary.getUpdatedAt()
        );
    }
}