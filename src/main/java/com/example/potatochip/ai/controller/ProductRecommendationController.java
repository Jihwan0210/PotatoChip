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
@RequestMapping("/api/products")
public class ProductRecommendationController {

    private final ProductRecommendationService ProductRecommendationService;

    @GetMapping("/{productId}/related-recommendations")
    public ResponseEntity<?> getRelatedRecommendations(@PathVariable Long productId) {
        try {
            List<ProductRecommendationDTO> recommendations =
                    ProductRecommendationService.getProducts(productId);

            return ResponseEntity.ok(recommendations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
