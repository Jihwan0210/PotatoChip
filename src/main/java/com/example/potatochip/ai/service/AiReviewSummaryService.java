package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.AiReviewSummaryDTO;

public interface AiReviewSummaryService {

    AiReviewSummaryDTO getAiReviewSummaryByProductId(Long productId);

    AiReviewSummaryDTO saveOrUpdateAiReviewSummary(Long productId, AiReviewSummaryDTO aiReviewSummaryDTO);

    AiReviewSummaryDTO updateAiReviewSummary(Long productId, AiReviewSummaryDTO aiReviewSummaryDTO);

    void deleteAiReviewSummary(Long productId);
}