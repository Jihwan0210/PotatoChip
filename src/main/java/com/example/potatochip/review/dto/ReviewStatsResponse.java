package com.example.potatochip.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewStatsResponse {

    private Long totalReviewCount;
    private Double averageRating;
    private Double repurchaseRate;
    private Long photoReviewCount;
}