package com.example.potatochip.review.dto;

public class ReviewStatsResponse {

    private Long totalReviewCount;
    private Double averageRating;
    private Double repurchaseRate;
    private Long photoReviewCount;

    public ReviewStatsResponse(
            Long totalReviewCount,
            Double averageRating,
            Double repurchaseRate,
            Long photoReviewCount
    ) {
        this.totalReviewCount = totalReviewCount;
        this.averageRating = averageRating;
        this.repurchaseRate = repurchaseRate;
        this.photoReviewCount = photoReviewCount;
    }

    public Long getTotalReviewCount() {
        return totalReviewCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Double getRepurchaseRate() {
        return repurchaseRate;
    }

    public Long getPhotoReviewCount() {
        return photoReviewCount;
    }
}