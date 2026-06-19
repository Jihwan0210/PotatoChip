package com.example.potatochip.product.controller;

import com.example.potatochip.product.dto.RelatedProductRecommendationDTO;
import com.example.potatochip.product.service.RelatedProductRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class RelatedProductRecommendationController {

    private final RelatedProductRecommendationService relatedProductRecommendationService;

    @GetMapping("/{productId}/related-recommendations")
    public ResponseEntity<?> getRelatedRecommendations(@PathVariable Long productId) {
        try {
            List<RelatedProductRecommendationDTO> recommendations =
                    relatedProductRecommendationService.getProducts(productId);

            return ResponseEntity.ok(recommendations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
