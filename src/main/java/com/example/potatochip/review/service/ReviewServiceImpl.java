package com.example.potatochip.review.service;

import com.example.potatochip.ai.service.AiReviewSummaryAutoService;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.review.dto.ReviewDTO;
import com.example.potatochip.review.dto.ReviewHelpfulDTO;
import com.example.potatochip.review.dto.ReviewStatsDTO;
import com.example.potatochip.review.entity.Review;
import com.example.potatochip.review.entity.ReviewHelpful;
import com.example.potatochip.review.repository.ReviewHelpfulRepository;
import com.example.potatochip.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewHelpfulRepository reviewHelpfulRepository;
    private final ProductRepository productRepository;
    private final AiReviewSummaryAutoService aiReviewSummaryAutoService;

    @Override
    public List<ReviewDTO> getReviewsByProductId(Long productId, Long userId) {
        return reviewRepository.findByProductIdAndIsActiveTrueOrderByCreatedAtDesc(productId)
                .stream()
                .map(review -> {
                    Long helpfulCount = reviewHelpfulRepository.countByReviewId(review.getReviewId());
                    Boolean helpfulByCurrentUser = userId != null &&
                            reviewHelpfulRepository.existsByReviewIdAndUserId(review.getReviewId(), userId);

                    return ReviewDTO.fromEntity(review, helpfulCount, helpfulByCurrentUser);
                })
                .toList();
    }

    @Override
    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        validateRating(reviewDTO.getRating());
        validateContent(reviewDTO.getContent());

        Product product = findProduct(reviewDTO.getProductId());

        if (reviewDTO.getOrderItemId() != null &&
                reviewRepository.existsByOrderItemIdAndUserIdAndIsActiveTrue(
                        reviewDTO.getOrderItemId(),
                        reviewDTO.getUserId()
                )) {
            throw new IllegalArgumentException("이미 리뷰를 작성한 주문 상품입니다.");
        }

        Review review = Review.builder()
                .product(product)
                .userId(reviewDTO.getUserId())
                .orderItemId(reviewDTO.getOrderItemId())
                .rating(reviewDTO.getRating())
                .content(reviewDTO.getContent())
                .imageUrl(normalizeImageUrl(reviewDTO.getImageUrl()))
                .repurchaseIntent(Boolean.TRUE.equals(reviewDTO.getRepurchaseIntent()))
                .isAnonymous(Boolean.TRUE.equals(reviewDTO.getIsAnonymous()))
                .build();

        Review savedReview = reviewRepository.save(review);

        refreshAiSummarySafely(savedReview.getProductId());

        return ReviewDTO.fromEntity(savedReview, 0L, false);
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDTO) {
        validateRating(reviewDTO.getRating());
        validateContent(reviewDTO.getContent());

        Review review = reviewRepository.findByReviewIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 리뷰가 없습니다."));

        if (!review.isWrittenBy(reviewDTO.getUserId())) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.updateReview(
                reviewDTO.getRating(),
                reviewDTO.getContent(),
                normalizeImageUrl(reviewDTO.getImageUrl()),
                Boolean.TRUE.equals(reviewDTO.getRepurchaseIntent()),
                Boolean.TRUE.equals(reviewDTO.getIsAnonymous())
        );

        refreshAiSummarySafely(review.getProductId());

        Long helpfulCount = reviewHelpfulRepository.countByReviewId(review.getReviewId());
        Boolean helpfulByCurrentUser = reviewHelpfulRepository.existsByReviewIdAndUserId(
                review.getReviewId(),
                reviewDTO.getUserId()
        );

        return ReviewDTO.fromEntity(review, helpfulCount, helpfulByCurrentUser);
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
    public ReviewStatsDTO getReviewStatsByProductId(Long productId) {
        Long totalReviewCount = reviewRepository.countByProductIdAndIsActiveTrue(productId);
        Long repurchaseCount = reviewRepository.countByProductIdAndIsActiveTrueAndRepurchaseIntentTrue(productId);
        Long photoReviewCount = reviewRepository.countPhotoReviewsByProductId(productId);
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);

        double safeAverageRating = averageRating == null ? 0.0 : roundToOneDecimal(averageRating);
        double repurchaseRate = totalReviewCount == 0 ? 0.0 :
                roundToOneDecimal((repurchaseCount * 100.0) / totalReviewCount);

        return new ReviewStatsDTO(
                totalReviewCount,
                safeAverageRating,
                repurchaseRate,
                photoReviewCount
        );
    }

    @Override
    @Transactional
    public ReviewHelpfulDTO addHelpful(Long reviewId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인 후 도움돼요를 누를 수 있습니다.");
        }

        Review review = reviewRepository.findByReviewIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("도움돼요를 누를 리뷰가 없습니다."));

        boolean helpfulByCurrentUser;

        var existingHelpful = reviewHelpfulRepository.findByReviewIdAndUserId(reviewId, userId);

        if (existingHelpful.isPresent()) {
            reviewHelpfulRepository.delete(existingHelpful.get());
            helpfulByCurrentUser = false;
        } else {
            reviewHelpfulRepository.save(new ReviewHelpful(review, userId));
            helpfulByCurrentUser = true;
        }

        Long helpfulCount = reviewHelpfulRepository.countByReviewId(reviewId);

        return new ReviewHelpfulDTO(
                reviewId,
                helpfulCount,
                helpfulByCurrentUser
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
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    aiReviewSummaryAutoService.refreshAiReviewSummary(productId);
                }
            });
            return;
        }

        aiReviewSummaryAutoService.refreshAiReviewSummary(productId);
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

    @Override
    public List<ReviewDTO> getMyReviews(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }

        return reviewRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(review -> {
                    Long helpfulCount = reviewHelpfulRepository.countByReviewId(review.getReviewId());
                    Boolean helpfulByCurrentUser = reviewHelpfulRepository.existsByReviewIdAndUserId(
                            review.getReviewId(),
                            userId
                    );

                    return ReviewDTO.fromEntity(review, helpfulCount, helpfulByCurrentUser);
                })
                .toList();
    }
}