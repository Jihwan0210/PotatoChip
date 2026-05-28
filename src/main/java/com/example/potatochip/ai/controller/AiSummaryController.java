package com.example.potatochip.ai.controller;

import com.example.potatochip.ai.dto.AiReviewSummaryDTO;
import com.example.potatochip.ai.dto.AiReviewSummarySaveRequest;
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
    @PostMapping("/{productId}/ai-review-summary")
    public ResponseEntity<AiReviewSummaryDTO> saveAiReviewSummary(
            @PathVariable Long productId,
            @RequestBody AiReviewSummarySaveRequest request
    ) {
        AiReviewSummaryDTO aiReviewSummaryDTO =
                aiReviewSummaryService.saveOrUpdateAiReviewSummary(productId, request);

        return ResponseEntity.ok(aiReviewSummaryDTO);
    }
    @PutMapping("/{productId}/ai-review-summary")
    public ResponseEntity<?> updateAiReviewSummary(
            @PathVariable Long productId,
            @RequestBody AiReviewSummarySaveRequest request
    ) {
        try {
            AiReviewSummaryDTO aiReviewSummaryDTO =
                    aiReviewSummaryService.updateAiReviewSummary(productId, request);

            return ResponseEntity.ok(aiReviewSummaryDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(
                    Map.of("message", e.getMessage())
            );
        }
    }
    @DeleteMapping("/{productId}/ai-review-summary")
    public ResponseEntity<?> deleteAiReviewSummary(@PathVariable Long productId) {
        try {
            aiReviewSummaryService.deleteAiReviewSummary(productId);

            return ResponseEntity.ok(
                    Map.of("message", "AI 리뷰 총평이 비활성화되었습니다.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}