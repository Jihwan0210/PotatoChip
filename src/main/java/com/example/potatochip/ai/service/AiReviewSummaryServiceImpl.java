package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.AiReviewSummaryDTO;
import com.example.potatochip.ai.entity.AiReviewSummary;
import com.example.potatochip.ai.repository.AiReviewSummaryRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiReviewSummaryServiceImpl implements AiReviewSummaryService {

    private final AiReviewSummaryRepository aiReviewSummaryRepository;
    private final ProductRepository productRepository;

    @Override
    public AiReviewSummaryDTO getAiReviewSummaryByProductId(Long productId) {
        AiReviewSummary aiReviewSummary = aiReviewSummaryRepository
                .findByProductIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품의 AI 리뷰 총평이 없습니다."));

        return AiReviewSummaryDTO.fromEntity(aiReviewSummary);
    }

    @Override
    @Transactional
    public AiReviewSummaryDTO saveOrUpdateAiReviewSummary(Long productId, AiReviewSummaryDTO aiReviewSummaryDTO) {
        Product product = findProduct(productId);

        AiReviewSummary aiReviewSummary = aiReviewSummaryRepository
                .findByProductId(productId)
                .map(existingSummary -> {
                    existingSummary.updateSummary(
                            aiReviewSummaryDTO.getSummary(),
                            aiReviewSummaryDTO.getReviewCount()
                    );
                    return existingSummary;
                })
                .orElseGet(() -> new AiReviewSummary(
                        product,
                        aiReviewSummaryDTO.getSummary(),
                        aiReviewSummaryDTO.getReviewCount()
                ));

        AiReviewSummary savedSummary = aiReviewSummaryRepository.save(aiReviewSummary);

        return AiReviewSummaryDTO.fromEntity(savedSummary);
    }

    @Override
    @Transactional
    public AiReviewSummaryDTO updateAiReviewSummary(Long productId, AiReviewSummaryDTO aiReviewSummaryDTO) {
        AiReviewSummary aiReviewSummary = aiReviewSummaryRepository
                .findByProductIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 AI 리뷰 총평이 없습니다."));

        aiReviewSummary.updateSummary(
                aiReviewSummaryDTO.getSummary(),
                aiReviewSummaryDTO.getReviewCount()
        );

        return AiReviewSummaryDTO.fromEntity(aiReviewSummary);
    }

    @Override
    @Transactional
    public void deleteAiReviewSummary(Long productId) {
        AiReviewSummary aiReviewSummary = aiReviewSummaryRepository
                .findByProductIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 AI 리뷰 총평이 없습니다."));

        aiReviewSummary.deactivate();
    }

    private Product findProduct(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID가 필요합니다.");
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
    }
}