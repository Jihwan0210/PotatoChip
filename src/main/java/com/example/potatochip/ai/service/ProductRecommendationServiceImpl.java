package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.ProductRecommendationDTO;
import com.example.potatochip.ai.entity.ProductRecommendation;
import com.example.potatochip.ai.entity.ProductRecommendationType;
import com.example.potatochip.ai.ollama.OllamaClient;
import com.example.potatochip.ai.repository.ProductRecommendationRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    private static final int RECOMMENDATION_LIMIT = 8;

    private final ProductRecommendationRepository productRecommendationRepository;
    private final ProductRepository productRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final OllamaClient ollamaClient;

    /**
     * 기존 API 호환용
     * 기존 /api/ai/recommendations/generate 요청은 AI 추천 생성으로 처리
     */
    @Override
    @Transactional
    public List<ProductRecommendationDTO> generateRecommendations(ProductRecommendationDTO request) {
        return generateAiRecommendations(request);
    }

    /**
     * 기존 API 호환용
     * 기존 /api/ai/recommendations 요청은 AI 추천 조회로 처리
     */
    @Override
    public List<ProductRecommendationDTO> getRecommendations(Long userId, String sessionId) {
        return getAiRecommendations(userId, sessionId);
    }

    /**
     * AI 추천 생성
     * 구매 이력 기반 후보 상품 선정 + Ollama 추천 이유 생성
     */
    @Override
    @Transactional
    public List<ProductRecommendationDTO> generateAiRecommendations(ProductRecommendationDTO request) {
        validateRequest(request);

        Long userId = request.getUserId();
        String sessionId = request.getSessionId();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        if (userId != null) {
            productRecommendationRepository.deleteByUserIdAndType(userId, ProductRecommendationType.AI);
        } else {
            productRecommendationRepository.deleteBySessionIdAndType(sessionId, ProductRecommendationType.AI);
        }

        List<Long> purchasedProductIds = userId == null ? List.of() : findPurchasedProductIds(userId);
        List<String> purchasedCategories = userId == null ? List.of() : findPurchasedCategories(userId);

        LinkedHashSet<Long> recommendedProductIds = new LinkedHashSet<>();

        if (!purchasedCategories.isEmpty()) {
            recommendedProductIds.addAll(
                    findProductsByPurchaseHistory(
                            purchasedCategories,
                            purchasedProductIds,
                            RECOMMENDATION_LIMIT
                    )
            );
        }

        if (recommendedProductIds.size() < RECOMMENDATION_LIMIT) {
            recommendedProductIds.addAll(
                    findPopularProducts(
                            purchasedProductIds,
                            RECOMMENDATION_LIMIT - recommendedProductIds.size()
                    )
            );
        }

        List<ProductRecommendation> recommendations = new ArrayList<>();
        int rank = 1;

        for (Long productId : recommendedProductIds) {
            if (rank > RECOMMENDATION_LIMIT) {
                break;
            }

            Product product = productRepository.findById(productId).orElse(null);

            if (product == null) {
                continue;
            }

            String reason = generateAiReason(product, purchasedCategories);
            BigDecimal score = calculateScore(rank, ProductRecommendationType.AI);

            recommendations.add(new ProductRecommendation(
                    userId,
                    sessionId,
                    product,
                    ProductRecommendationType.AI,
                    reason,
                    score,
                    rank,
                    expiresAt
            ));

            rank++;
        }

        List<ProductRecommendation> savedRecommendations =
                productRecommendationRepository.saveAll(recommendations);

        return savedRecommendations.stream()
                .map(ProductRecommendationDTO::fromEntity)
                .toList();
    }

    /**
     * AI 추천 조회
     */
    @Override
    public List<ProductRecommendationDTO> getAiRecommendations(Long userId, String sessionId) {
        LocalDateTime now = LocalDateTime.now();

        if (userId != null) {
            return productRecommendationRepository
                    .findByUserIdAndTypeAndExpiresAtAfterOrderByRankOrderAsc(
                            userId,
                            ProductRecommendationType.AI,
                            now
                    )
                    .stream()
                    .map(ProductRecommendationDTO::fromEntity)
                    .toList();
        }

        if (sessionId != null && !sessionId.isBlank()) {
            return productRecommendationRepository
                    .findBySessionIdAndTypeAndExpiresAtAfterOrderByRankOrderAsc(
                            sessionId,
                            ProductRecommendationType.AI,
                            now
                    )
                    .stream()
                    .map(ProductRecommendationDTO::fromEntity)
                    .toList();
        }

        return List.of();
    }

    /**
     * 인기상품 추천 생성
     * AI 사용 안 함
     * product_rankings + wishlists + reviews 기반 점수 계산
     */
    @Override
    @Transactional
    public List<ProductRecommendationDTO> generatePopularRecommendations() {
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(6);

        productRecommendationRepository.deleteByType(ProductRecommendationType.POPULAR);

        List<Long> popularProductIds = findPopularProducts(List.of(), RECOMMENDATION_LIMIT);

        List<ProductRecommendation> recommendations = new ArrayList<>();
        int rank = 1;

        for (Long productId : popularProductIds) {
            if (rank > RECOMMENDATION_LIMIT) {
                break;
            }

            Product product = productRepository.findById(productId).orElse(null);

            if (product == null) {
                continue;
            }

            recommendations.add(new ProductRecommendation(
                    null,
                    null,
                    product,
                    ProductRecommendationType.POPULAR,
                    "최근 주문과 관심이 많은 인기 상품이에요.",
                    calculateScore(rank, ProductRecommendationType.POPULAR),
                    rank,
                    expiresAt
            ));

            rank++;
        }

        List<ProductRecommendation> savedRecommendations =
                productRecommendationRepository.saveAll(recommendations);

        return savedRecommendations.stream()
                .map(ProductRecommendationDTO::fromEntity)
                .toList();
    }

    /**
     * 인기상품 추천 조회
     * 저장된 인기추천이 없거나 만료되었으면 새로 생성
     */
    @Override
    @Transactional
    public List<ProductRecommendationDTO> getPopularRecommendations() {
        LocalDateTime now = LocalDateTime.now();

        List<ProductRecommendationDTO> recommendations = productRecommendationRepository
                .findByTypeAndExpiresAtAfterOrderByRankOrderAsc(
                        ProductRecommendationType.POPULAR,
                        now
                )
                .stream()
                .map(ProductRecommendationDTO::fromEntity)
                .toList();

        if (recommendations.isEmpty()) {
            return generatePopularRecommendations();
        }

        return recommendations;
    }

    /**
     * 사용자가 구매했던 상품 ID 목록
     * 이미 구매한 상품은 추천에서 제외하기 위한 용도
     */
    private List<Long> findPurchasedProductIds(Long userId) {
        String sql = """
                select distinct oi.product_id
                from orders o
                join order_items oi on o.id = oi.order_id
                where o.buyer_id = :userId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        return namedParameterJdbcTemplate.queryForList(sql, params, Long.class);
    }

    /**
     * 사용자가 구매했던 상품 카테고리 상위 3개
     * AI 추천 후보 추출 기준
     */
    private List<String> findPurchasedCategories(Long userId) {
        String sql = """
                select p.category
                from orders o
                join order_items oi on o.id = oi.order_id
                join products p on oi.product_id = p.id
                where o.buyer_id = :userId
                  and o.status in ('DELIVERED')
                group by p.category
                order by count(*) desc
                limit 3
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        return namedParameterJdbcTemplate.queryForList(sql, params, String.class);
    }

    /**
     * 구매 이력 카테고리와 같은 상품 추천
     */
    private List<Long> findProductsByPurchaseHistory(
            List<String> categories,
            List<Long> purchasedProductIds,
            int limit
    ) {
        if (categories == null || categories.isEmpty() || limit <= 0) {
            return List.of();
        }

        String sql = """
                select p.id
                from products p
                where p.category in (:categories)
                  and p.id not in (:purchasedProductIds)
                order by p.created_at desc
                limit :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("categories", categories)
                .addValue("purchasedProductIds", normalizeIds(purchasedProductIds))
                .addValue("limit", limit);

        return namedParameterJdbcTemplate.queryForList(sql, params, Long.class);
    }

    /**
     * 인기상품 추천 후보 조회
     * product_rankings, wishlists, reviews 기반 점수 계산
     */
    private List<Long> findPopularProducts(List<Long> purchasedProductIds, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        String sql = """
                select p.id
                from products p
                left join product_rankings pr
                       on pr.product_id = p.id
                      and pr.period_type = 'WEEKLY'
                left join (
                    select product_id, count(*) as wish_count
                    from wishlists
                    group by product_id
                ) w on w.product_id = p.id
                left join (
                    select product_id,
                           count(*) as review_count,
                           avg(rating) as avg_rating
                    from reviews
                    where is_active = 1
                      and is_hidden = 0
                    group by product_id
                ) r on r.product_id = p.id
                where p.id not in (:purchasedProductIds)
                group by p.id
                order by
                    (
                        coalesce(max(pr.sales_count), 0) * 0.45
                        + coalesce(max(w.wish_count), 0) * 0.25
                        + coalesce(max(r.review_count), 0) * 0.15
                        + coalesce(max(r.avg_rating), 0) * 0.15
                    ) desc,
                    p.created_at desc
                limit :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("purchasedProductIds", normalizeIds(purchasedProductIds))
                .addValue("limit", limit);

        return namedParameterJdbcTemplate.queryForList(sql, params, Long.class);
    }

    /**
     * Ollama를 사용해서 AI 추천 이유 생성
     */
    private String generateAiReason(Product product, List<String> purchasedCategories) {
        String fallbackReason = buildFallbackReason(product, purchasedCategories);

        // 구매 이력이 없으면 Ollama 호출하지 않음
        // 구매 이력 없음 상태에서 AI를 부르면 이상한 문장이 자주 나옴
        if (purchasedCategories == null || purchasedCategories.isEmpty()) {
            return fallbackReason;
        }

        try {
            String categoriesText = String.join(", ", purchasedCategories);

            String priceText = product.getDiscountPrice() != null
                    ? product.getDiscountPrice().toPlainString()
                    : product.getPrice().toPlainString();

            String prompt = """
        사용자의 구매 카테고리와 추천 상품 정보를 보고 추천 이유를 작성해.

        사용자 구매 카테고리: %s

        추천 상품 정보:
        상품명: %s
        상품 카테고리: %s
        원산지: %s
        가격: %s원

        작성 규칙:
        - 반드시 한국어만 사용
        - 추천 이유 한 문장만 출력
        - 50자 이내
        - 따옴표, 번호, 목록, 마크다운 사용 금지
        - "추천 이유:" 같은 접두사 사용 금지
        - 신선한, 맛있는, 고품질, 최고, 가장 좋다 같은 품질 평가 금지
        - 건강, 효능, 영양 같은 의학적 표현 금지
        - 가격이 저렴하다, 합리적이다 같은 가격 평가 금지
        - 원산지만으로 품질을 판단하지 말 것
        - 사용자의 구매 카테고리와 추천 상품 카테고리의 연관성만 설명

        좋은 예시:
        자주 구매한 채소류와 연관된 상품이라 추천해요.

        나쁜 예시:
        강원도 원산지의 신선하고 맛있는 감자라 추천해요.

        출력:
        """.formatted(
                    categoriesText,
                    product.getName(),
                    product.getCategory(),
                    product.getOrigin(),
                    priceText
            );

            String reason = ollamaClient.recommendProductReason(prompt);
            return cleanAiReason(reason, fallbackReason);

        } catch (Exception e) {
            return fallbackReason;
        }
    }

    private String buildFallbackReason(Product product, List<String> purchasedCategories) {
        if (product == null) {
            return "구매 이력을 바탕으로 추천된 상품이에요.";
        }

        if (purchasedCategories == null || purchasedCategories.isEmpty()) {
            return "최근 관심이 많은 " + product.getCategory() + " 상품이라 추천해요.";
        }

        if (purchasedCategories.contains(product.getCategory())) {
            return "자주 구매한 " + product.getCategory() + "류와 연관된 상품이라 추천해요.";
        }

        return "최근 구매 취향과 비슷한 상품이라 추천해요.";
    }

    private String cleanAiReason(String reason, String fallbackReason) {
        if (reason == null || reason.isBlank()) {
            return fallbackReason;
        }

        String cleaned = reason
                .replaceAll("[\"“”‘’]", "")
                .replaceAll("\\R", " ")
                .replace("추천 이유:", "")
                .replace("추천이유:", "")
                .replace("출력:", "")
                .trim();

        // 영어가 섞이면 기본 문구 사용
        if (cleaned.matches(".*[A-Za-z]{3,}.*")) {
            return fallbackReason;
        }

        // 중국어 한자가 섞이면 기본 문구 사용
        if (cleaned.matches(".*\\p{IsHan}.*")) {
            return fallbackReason;
        }

        // 근거 없는 품질/과장 표현 차단
        List<String> bannedWords = List.of(
                "가장 좋",
                "최고",
                "신선",
                "맛있",
                "고품질",
                "건강",
                "효능",
                "영양",
                "저렴",
                "합리적",
                "품질",
                "우수"
        );

        for (String bannedWord : bannedWords) {
            if (cleaned.contains(bannedWord)) {
                return fallbackReason;
            }
        }

        if (cleaned.length() > 60) {
            cleaned = cleaned.substring(0, 60).trim();
        }

        if (cleaned.isBlank()) {
            return fallbackReason;
        }

        return cleaned;
    }
    /**
     * purchasedProductIds가 비어있을 때 SQL not in 에러 방지용
     */
    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of(-1L);
        }

        return ids;
    }

    /**
     * 추천 점수 계산
     */
    private BigDecimal calculateScore(int rank, ProductRecommendationType type) {
        double baseScore = type == ProductRecommendationType.AI ? 1.0 : 0.85;
        double score = Math.max(0.1, baseScore - ((rank - 1) * 0.05));

        return BigDecimal.valueOf(score)
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 요청 검증
     */
    private void validateRequest(ProductRecommendationDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("추천 요청 정보가 필요합니다.");
        }

        if (request.getUserId() == null
                && (request.getSessionId() == null || request.getSessionId().isBlank())) {
            throw new IllegalArgumentException("사용자 ID 또는 세션 ID가 필요합니다.");
        }
    }
}