package com.example.potatochip.ai.controller;

import com.example.potatochip.ai.dto.AiReviewSummaryDTO;
import com.example.potatochip.ai.service.AiReviewSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AiReviewSummaryController {

    private final AiReviewSummaryService aiReviewSummaryService;

    @GetMapping("/api/products/{productId}/ai-review-summary")
    public ResponseEntity<?> getAiReviewSummary(
            @PathVariable Long productId
    ) {
        try {
            AiReviewSummaryDTO response = aiReviewSummaryService.getAiReviewSummaryByProductId(productId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/api/products/{productId}/ai-review-summary")
    public ResponseEntity<?> saveOrUpdateAiReviewSummary(
            @PathVariable Long productId,
            @RequestBody AiReviewSummaryDTO aiReviewSummaryDTO
    ) {
        try {
            AiReviewSummaryDTO response = aiReviewSummaryService.saveOrUpdateAiReviewSummary(
                    productId,
                    aiReviewSummaryDTO
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PutMapping("/api/products/{productId}/ai-review-summary")
    public ResponseEntity<?> updateAiReviewSummary(
            @PathVariable Long productId,
            @RequestBody AiReviewSummaryDTO aiReviewSummaryDTO
    ) {
        try {
            AiReviewSummaryDTO response = aiReviewSummaryService.updateAiReviewSummary(
                    productId,
                    aiReviewSummaryDTO
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @DeleteMapping("/api/products/{productId}/ai-review-summary")
    public ResponseEntity<?> deleteAiReviewSummary(
            @PathVariable Long productId
    ) {
        try {
            aiReviewSummaryService.deleteAiReviewSummary(productId);
            return ResponseEntity.ok(
                    Map.of("message", "AI 리뷰 총평이 삭제되었습니다.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}