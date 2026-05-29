package com.example.potatochip.review.service;

import com.example.potatochip.ai.service.AiReviewSummaryAutoService;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.review.dto.ReviewCreateRequest;
import com.example.potatochip.review.dto.ReviewResponse;
import com.example.potatochip.review.dto.ReviewStatsResponse;
import com.example.potatochip.review.dto.ReviewUpdateRequest;
import com.example.potatochip.review.entity.Review;
import com.example.potatochip.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final AiReviewSummaryAutoService aiReviewSummaryAutoService;

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

        Product product = findProduct(request.getProductId());

        Review review = Review.builder()
                .product(product)
                .userId(request.getUserId())
                .orderItemId(request.getOrderItemId())
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrl(normalizeImageUrl(request.getImageUrl()))
                .repurchaseIntent(Boolean.TRUE.equals(request.getRepurchaseIntent()))
                .build();

        Review savedReview = reviewRepository.save(review);

        refreshAiSummarySafely(savedReview.getProductId());

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

        refreshAiSummarySafely(review.getProductId());

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

        Long productId = review.getProductId();

        review.deactivate();

        refreshAiSummarySafely(productId);
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

    private Product findProduct(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID가 필요합니다.");
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
    }

    private void refreshAiSummarySafely(Long productId) {
        try {
            aiReviewSummaryAutoService.refreshAiReviewSummary(productId);
        } catch (RuntimeException e) {
            log.warn("AI 리뷰 총평 자동 갱신 실패. productId={}", productId, e);
        }
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