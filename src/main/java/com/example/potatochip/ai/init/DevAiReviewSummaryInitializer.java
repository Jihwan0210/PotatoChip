package com.example.potatochip.ai.init;

import com.example.potatochip.ai.service.AiReviewSummaryAutoService;
import com.example.potatochip.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevAiReviewSummaryInitializer implements ApplicationRunner {

    private final ReviewRepository reviewRepository;
    private final AiReviewSummaryAutoService aiReviewSummaryAutoService;

    @Value("${app.dev.ai-summary-init.enabled:false}")
    private boolean enabled;

    @Value("${app.dev.ai-summary-init.min-review-count:10}")
    private long minReviewCount;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        List<Long> productIds = reviewRepository.findProductIdsHavingReviewCountAtLeast(minReviewCount);

        log.info("AI 리뷰총평 초기화 대상 상품 수: {}", productIds.size());

        for (Long productId : productIds) {
            try {
                aiReviewSummaryAutoService.refreshAiReviewSummarySync(productId);
                log.info("AI 리뷰총평 생성 완료 productId={}", productId);
            } catch (Exception e) {
                log.warn("AI 리뷰총평 생성 실패 productId={}", productId, e);
            }
        }
    }
}