package com.example.potatochip.ai.controller;

import com.example.potatochip.ai.dto.AiReviewSummaryDTO;
import com.example.potatochip.ai.service.AiReviewSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class AiSummaryController {

    private final AiReviewSummaryService aiReviewSummaryService;

    public AiSummaryController(AiReviewSummaryService aiReviewSummaryService) {
        this.aiReviewSummaryService = aiReviewSummaryService;
    }

    @GetMapping("/{productId}/ai-review-summary")
    public ResponseEntity<?> getAiReviewSummary(@PathVariable Long productId) {
        try {
            AiReviewSummaryDTO aiReviewSummaryDTO =
                    aiReviewSummaryService.getAiReviewSummaryByProductId(productId);

            return ResponseEntity.ok(aiReviewSummaryDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}