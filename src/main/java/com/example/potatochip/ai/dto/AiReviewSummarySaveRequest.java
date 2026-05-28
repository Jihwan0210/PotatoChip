package com.example.potatochip.ai.dto;

public class AiReviewSummarySaveRequest {

    private String summary;
    private Integer reviewCount;

    public AiReviewSummarySaveRequest() {
    }

    public String getSummary() {
        return summary;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }
}