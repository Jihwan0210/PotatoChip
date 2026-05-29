package com.example.potatochip.review.controller;

import com.example.potatochip.review.dto.ReviewCreateRequest;
import com.example.potatochip.review.dto.ReviewHelpfulResponse;
import com.example.potatochip.review.dto.ReviewResponse;
import com.example.potatochip.review.dto.ReviewStatsResponse;
import com.example.potatochip.review.dto.ReviewUpdateRequest;
import com.example.potatochip.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviewsByProductId(
            @RequestParam Long productId,
            @RequestParam(required = false) Long userId
    ) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProductId(productId, userId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestBody ReviewCreateRequest request
    ) {
        try {
            ReviewResponse reviewResponse = reviewService.createReview(request);
            return ResponseEntity.ok(reviewResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequest request
    ) {
        try {
            ReviewResponse reviewResponse = reviewService.updateReview(reviewId, request);
            return ResponseEntity.ok(reviewResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {
        try {
            reviewService.deleteReview(reviewId, userId);
            return ResponseEntity.ok(
                    Map.of("message", "리뷰가 삭제되었습니다.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ReviewStatsResponse> getReviewStatsByProductId(
            @RequestParam Long productId
    ) {
        ReviewStatsResponse reviewStats = reviewService.getReviewStatsByProductId(productId);
        return ResponseEntity.ok(reviewStats);
    }

    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<?> addHelpful(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {
        try {
            ReviewHelpfulResponse response = reviewService.addHelpful(reviewId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}