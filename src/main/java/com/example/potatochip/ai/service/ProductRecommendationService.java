package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.ProductRecommendationDTO;

import java.util.List;

public interface ProductRecommendationService {

    // 기존 호환용: AI 추천으로 처리
    List<ProductRecommendationDTO> generateRecommendations(ProductRecommendationDTO request);

    List<ProductRecommendationDTO> getRecommendations(Long userId, String sessionId);

    List<ProductRecommendationDTO> generateAiRecommendations(ProductRecommendationDTO request);

    List<ProductRecommendationDTO> getAiRecommendations(Long userId, String sessionId);

    List<ProductRecommendationDTO> generatePopularRecommendations();

    List<ProductRecommendationDTO> getPopularRecommendations();
}