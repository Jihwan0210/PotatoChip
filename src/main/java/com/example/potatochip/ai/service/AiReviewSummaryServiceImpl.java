package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.AiReviewSummaryDTO;
import com.example.potatochip.ai.entity.AiReviewSummary;
import com.example.potatochip.ai.repository.AiReviewSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AiReviewSummaryServiceImpl implements AiReviewSummaryService {

    private final AiReviewSummaryRepository aiReviewSummaryRepository;

    public AiReviewSummaryServiceImpl(AiReviewSummaryRepository aiReviewSummaryRepository) {
        this.aiReviewSummaryRepository = aiReviewSummaryRepository;
    }

    @Override
    public AiReviewSummaryDTO getAiReviewSummaryByProductId(Long productId) {
        AiReviewSummary aiReviewSummary = aiReviewSummaryRepository
                .findByProductIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품의 AI 리뷰 총평이 없습니다."));

        return AiReviewSummaryDTO.fromEntity(aiReviewSummary);
    }
}