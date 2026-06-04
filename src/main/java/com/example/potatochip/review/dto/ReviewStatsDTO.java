package com.example.potatochip.review.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewStatsDTO {

    private Long totalReviewCount;

    private Double averageRating;

    private Double repurchaseRate;

    private Long photoReviewCount;
}