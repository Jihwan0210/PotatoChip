package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.ProductRecommendationDTO;

import java.util.List;

public interface ProductRecommendationService {

    List<ProductRecommendationDTO> generateRecommendations(ProductRecommendationDTO request);

    List<ProductRecommendationDTO> getRecommendations(Long userId, String sessionId);
}