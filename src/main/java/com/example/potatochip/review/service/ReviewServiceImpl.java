package com.example.potatochip.review.service;

import com.example.potatochip.review.dto.ReviewCreateRequest;
import com.example.potatochip.review.dto.ReviewResponse;
import com.example.potatochip.review.dto.ReviewStatsResponse;
import com.example.potatochip.review.dto.ReviewUpdateRequest;
import com.example.potatochip.review.entity.Review;
import com.example.potatochip.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductIdAndIsActiveTrueOrderByCreatedAtDesc(productId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        validateRating(request.getRating());
        validateContent(request.getContent());

        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(request.getUserId())
                .orderItemId(request.getOrderItemId())
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrl(normalizeImageUrl(request.getImageUrl()))
                .repurchaseIntent(Boolean.TRUE.equals(request.getRepurchaseIntent()))
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.fromEntity(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request) {
        validateRating(request.getRating());
        validateContent(request.getContent());

        Review review = reviewRepository.findByReviewIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 리뷰가 없습니다."));

        if (!review.isWrittenBy(request.getUserId())) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.updateReview(
                request.getRating(),
                request.getContent(),
                normalizeImageUrl(request.getImageUrl()),
                Boolean.TRUE.equals(request.getRepurchaseIntent())
        );

        return ReviewResponse.fromEntity(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByReviewIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 리뷰가 없습니다."));

        if (!review.isWrittenBy(userId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        review.deactivate();
    }

    @Override
    public ReviewStatsResponse getReviewStatsByProductId(Long productId) {
        Long totalReviewCount = reviewRepository.countByProductIdAndIsActiveTrue(productId);
        Long repurchaseCount = reviewRepository.countByProductIdAndIsActiveTrueAndRepurchaseIntentTrue(productId);
        Long photoReviewCount = reviewRepository.countPhotoReviewsByProductId(productId);
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);

        double safeAverageRating = averageRating == null ? 0.0 : roundToOneDecimal(averageRating);
        double repurchaseRate = totalReviewCount == 0 ? 0.0 :
                roundToOneDecimal((repurchaseCount * 100.0) / totalReviewCount);

        return new ReviewStatsResponse(
                totalReviewCount,
                safeAverageRating,
                repurchaseRate,
                photoReviewCount
        );
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1점 이상 5점 이하만 입력할 수 있습니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }
    }

    private double roundToOneDecimal(Double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        return imageUrl.trim();
    }
}