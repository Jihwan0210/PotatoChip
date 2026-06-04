package com.example.potatochip.review.service;

import com.example.potatochip.review.dto.ReviewDTO;
import com.example.potatochip.review.dto.ReviewHelpfulDTO;
import com.example.potatochip.review.dto.ReviewStatsDTO;

import java.util.List;

public interface ReviewService {

    List<ReviewDTO> getReviewsByProductId(Long productId, Long userId);

    ReviewDTO createReview(ReviewDTO reviewDTO);

    ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDTO);

    void deleteReview(Long reviewId, Long userId);

    ReviewStatsDTO getReviewStatsByProductId(Long productId);

    ReviewHelpfulDTO addHelpful(Long reviewId, Long userId);
}