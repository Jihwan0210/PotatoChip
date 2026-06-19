package com.example.potatochip.ai.controller;

import com.example.potatochip.ai.dto.ProductRecommendationDTO;
import com.example.potatochip.ai.service.ProductRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/recommendations")
public class ProductRecommendationController {

    private final ProductRecommendationService productRecommendationService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateRecommendations(@RequestBody ProductRecommendationDTO request) {
        try {
            List<ProductRecommendationDTO> recommendations =
                    productRecommendationService.generateRecommendations(request);

            return ResponseEntity.ok(recommendations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductRecommendationDTO>> getRecommendations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId
    ) {
        return ResponseEntity.ok(
                productRecommendationService.getRecommendations(userId, sessionId)
        );
    }

    @PostMapping("/generate/ai")
    public ResponseEntity<?> generateAiRecommendations(@RequestBody ProductRecommendationDTO request) {
        try {
            List<ProductRecommendationDTO> recommendations =
                    productRecommendationService.generateAiRecommendations(request);

            return ResponseEntity.ok(recommendations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ai")
    public ResponseEntity<List<ProductRecommendationDTO>> getAiRecommendations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId
    ) {
        return ResponseEntity.ok(
                productRecommendationService.getAiRecommendations(userId, sessionId)
        );
    }

    @PostMapping("/generate/popular")
    public ResponseEntity<List<ProductRecommendationDTO>> generatePopularRecommendations() {
        return ResponseEntity.ok(
                productRecommendationService.generatePopularRecommendations()
        );
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ProductRecommendationDTO>> getPopularRecommendations() {
        return ResponseEntity.ok(
                productRecommendationService.getPopularRecommendations()
        );
    }
}
