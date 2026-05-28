package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.AiReviewSummaryDTO;
import com.example.potatochip.ai.dto.AiReviewSummarySaveRequest;

public interface AiReviewSummaryService {

    AiReviewSummaryDTO getAiReviewSummaryByProductId(Long productId);

    AiReviewSummaryDTO saveOrUpdateAiReviewSummary(Long productId, AiReviewSummarySaveRequest request);

    AiReviewSummaryDTO updateAiReviewSummary(Long productId, AiReviewSummarySaveRequest request);

    void deleteAiReviewSummary(Long productId);
}