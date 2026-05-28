package com.example.potatochip.review.service;

import com.example.potatochip.review.dto.ReviewCreateRequest;
import com.example.potatochip.review.dto.ReviewResponse;
import com.example.potatochip.review.dto.ReviewStatsResponse;
import com.example.potatochip.review.dto.ReviewUpdateRequest;

import java.util.List;

public interface ReviewService {

    List<ReviewResponse> getReviewsByProductId(Long productId);

    ReviewResponse createReview(ReviewCreateRequest request);

    ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request);

    void deleteReview(Long reviewId, Long userId);

    ReviewStatsResponse getReviewStatsByProductId(Long productId);
}