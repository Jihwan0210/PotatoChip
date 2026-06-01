package com.example.potatochip.ai.service;

import com.example.potatochip.ai.entity.AiReviewSummary;
import com.example.potatochip.ai.ollama.OllamaClient;
import com.example.potatochip.ai.repository.AiReviewSummaryRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.review.entity.Review;
import com.example.potatochip.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AiReviewSummaryAutoService {

    private static final int FIRST_SUMMARY_REVIEW_COUNT = 10;
    private static final int SUMMARY_REFRESH_INTERVAL = 5;

    private final ReviewRepository reviewRepository;
    private final AiReviewSummaryRepository aiReviewSummaryRepository;
    private final ProductRepository productRepository;
    private final OllamaClient ollamaClient;

    @Async
    public void refreshAiReviewSummary(Long productId) {
        try {
            refreshAiReviewSummaryInternal(productId);
        } catch (RuntimeException e) {
            log.warn("AI 리뷰 총평 비동기 갱신 실패. productId={}", productId, e);
        }
    }

    private void refreshAiReviewSummaryInternal(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndIsActiveTrueOrderByCreatedAtDesc(productId);
        int reviewCount = reviews.size();

        if (reviewCount < FIRST_SUMMARY_REVIEW_COUNT) {
            aiReviewSummaryRepository.findByProductIdAndIsActiveTrue(productId)
                    .ifPresent(AiReviewSummary::deactivate);
            return;
        }

        var activeSummary = aiReviewSummaryRepository.findByProductIdAndIsActiveTrue(productId);

        if (!shouldRefreshSummary(reviewCount, activeSummary.orElse(null))) {
            return;
        }

        Product product = findProduct(productId);
        String prompt = buildPrompt(product, reviews);
        String summary = generateKoreanSummary(prompt);

        AiReviewSummary aiReviewSummary = aiReviewSummaryRepository.findByProductId(productId)
                .map(existingSummary -> {
                    existingSummary.updateSummary(summary, reviewCount);
                    return existingSummary;
                })
                .orElseGet(() -> new AiReviewSummary(product, summary, reviewCount));

        aiReviewSummaryRepository.save(aiReviewSummary);
    }

    private boolean shouldRefreshSummary(int currentReviewCount, AiReviewSummary activeSummary) {
        if (activeSummary == null) {
            return currentReviewCount >= FIRST_SUMMARY_REVIEW_COUNT;
        }

        Integer savedReviewCount = activeSummary.getReviewCount();

        if (savedReviewCount == null) {
            return true;
        }

        if (currentReviewCount < savedReviewCount) {
            return true;
        }

        if (currentReviewCount == savedReviewCount) {
            return false;
        }

        return currentReviewCount % SUMMARY_REFRESH_INTERVAL == 0;
    }

    private String buildPrompt(Product product, List<Review> reviews) {
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        long repurchaseCount = reviews.stream()
                .filter(review -> Boolean.TRUE.equals(review.getRepurchaseIntent()))
                .count();

        long photoReviewCount = reviews.stream()
                .filter(review -> review.getImageUrl() != null && !review.getImageUrl().isBlank())
                .count();

        StringBuilder reviewTexts = new StringBuilder();

        reviews.stream()
                .limit(10)
                .forEach(review -> reviewTexts
                        .append("- 별점 ")
                        .append(review.getRating())
                        .append("점: ")
                        .append(limitText(review.getContent()))
                        .append("\n"));

        return """
                당신은 한국어 쇼핑몰의 AI 리뷰 총평 작성자입니다.

                반드시 지켜야 할 규칙:
                1. 답변은 반드시 자연스러운 한국어로만 작성하세요.
                2. 영어, 외국어, 한자, 번역투 표현을 절대 사용하지 마세요.
                3. satisfaction, review, summary 같은 영어 단어를 절대 사용하지 마세요.
                4. 이 상품의 총평은 같은 딱딱한 문구로 시작하지 마세요.
                5. 따옴표를 사용하지 마세요.
                6. 리뷰에 없는 내용을 추측하지 마세요.
                7. 쇼핑몰 리뷰 총평처럼 2~3문장으로 자연스럽게 작성하세요.
                8. 최종 총평 문장만 출력하세요.

                상품 ID: %d
                상품명: %s
                카테고리: %s
                원산지: %s
                리뷰 수: %d개
                평균 별점: %.1f점
                재구매 의향 리뷰 수: %d개
                사진 리뷰 수: %d개

                실제 리뷰 내용:
                %s
                """.formatted(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getOrigin(),
                reviews.size(),
                averageRating,
                repurchaseCount,
                photoReviewCount,
                reviewTexts.toString()
        );
    }

    private Product findProduct(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID가 필요합니다.");
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
    }

    private String generateKoreanSummary(String prompt) {
        String summary = cleanSummary(ollamaClient.chat(prompt));

        if (containsEnglish(summary)) {
            String retryPrompt = prompt + """

                    이전 응답에 영어 또는 외국어가 섞였습니다.
                    다시 작성하세요.
                    반드시 한국어만 사용하고, 영어 단어를 하나도 포함하지 마세요.
                    자연스러운 쇼핑몰 리뷰 총평 1문장만 출력하세요.
                    """;

            summary = cleanSummary(ollamaClient.chat(retryPrompt));
        }

        return summary;
    }

    private boolean containsEnglish(String text) {
        if (text == null) {
            return false;
        }

        return text.matches(".*[A-Za-z].*");
    }

    private String limitText(String content) {
        if (content == null) {
            return "";
        }

        String trimmed = content.trim();

        if (trimmed.length() <= 180) {
            return trimmed;
        }

        return trimmed.substring(0, 180);
    }

    private String cleanSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            return "등록된 리뷰를 바탕으로 AI 리뷰 총평을 생성하지 못했습니다.";
        }

        String cleaned = summary
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\"", "")
                .replace("이 상품의 총평은", "")
                .replace("요약하면", "")
                .trim();

        if (cleaned.length() <= 500) {
            return cleaned;
        }

        return cleaned.substring(0, 500);
    }
}