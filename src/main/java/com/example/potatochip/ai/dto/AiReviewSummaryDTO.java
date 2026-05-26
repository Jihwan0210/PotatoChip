package com.example.potatochip.ai.dto;

import com.example.potatochip.ai.entity.AiReviewSummary;

import java.time.LocalDateTime;

public class AiReviewSummaryDTO {

    private Long aiReviewSummaryId;
    private Long productId;
    private String summary;
    private Integer reviewCount;
    private Boolean isActive;
    private LocalDateTime generatedAt;
    private LocalDateTime updatedAt;

    public AiReviewSummaryDTO() {
    }

    public AiReviewSummaryDTO(
            Long aiReviewSummaryId,
            Long productId,
            String summary,
            Integer reviewCount,
            Boolean isActive,
            LocalDateTime generatedAt,
            LocalDateTime updatedAt
    ) {
        this.aiReviewSummaryId = aiReviewSummaryId;
        this.productId = productId;
        this.summary = summary;
        this.reviewCount = reviewCount;
        this.isActive = isActive;
        this.generatedAt = generatedAt;
        this.updatedAt = updatedAt;
    }

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

    public Long getAiReviewSummaryId() {
        return aiReviewSummaryId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSummary() {
        return summary;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}