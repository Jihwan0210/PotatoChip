package com.example.potatochip.review.controller;

import com.example.potatochip.review.dto.ReviewDTO;
import com.example.potatochip.review.dto.ReviewHelpfulDTO;
import com.example.potatochip.review.dto.ReviewStatsDTO;
import com.example.potatochip.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping("/api/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviewsByProductId(
            @RequestParam Long productId,
            @RequestParam(required = false) Long userId
    ) {
        List<ReviewDTO> reviews = reviewService.getReviewsByProductId(productId, userId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/api/reviews")
    public ResponseEntity<?> createReview(
            @RequestBody ReviewDTO reviewDTO
    ) {
        try {
            ReviewDTO reviewResponse = reviewService.createReview(reviewDTO);
            return ResponseEntity.ok(reviewResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PutMapping("/api/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewDTO reviewDTO
    ) {
        try {
            ReviewDTO reviewResponse = reviewService.updateReview(reviewId, reviewDTO);
            return ResponseEntity.ok(reviewResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @DeleteMapping("/api/reviews/{reviewId}")
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

    @GetMapping("/api/reviews/stats")
    public ResponseEntity<ReviewStatsDTO> getReviewStatsByProductId(
            @RequestParam Long productId
    ) {
        ReviewStatsDTO reviewStats = reviewService.getReviewStatsByProductId(productId);
        return ResponseEntity.ok(reviewStats);
    }

    @PostMapping("/api/reviews/{reviewId}/helpful")
    public ResponseEntity<?> addHelpful(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {
        try {
            ReviewHelpfulDTO response = reviewService.addHelpful(reviewId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/api/reviews/my")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        try {
            Long loginUserId = getLoginUserId(authentication);
            List<ReviewDTO> reviews = reviewService.getMyReviews(loginUserId);
            return ResponseEntity.ok(reviews);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    private Long getLoginUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        String email = String.valueOf(authentication.getPrincipal());

        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("로그인 사용자를 찾을 수 없습니다."));

        return user.getId();
    }
}