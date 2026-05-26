package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.AiReviewSummaryDTO;

public interface AiReviewSummaryService {

    AiReviewSummaryDTO getAiReviewSummaryByProductId(Long productId);
}