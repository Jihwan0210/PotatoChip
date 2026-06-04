package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.ProductRecommendationDTO;
import com.example.potatochip.ai.entity.ProductRecommendation;
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

    @Override
    @Transactional
    public List<ProductRecommendationDTO> generateRecommendations(ProductRecommendationDTO request) {
        validateRequest(request);

        Long userId = request.getUserId();
        String sessionId = request.getSessionId();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        if (userId != null) {
            productRecommendationRepository.deleteByUserId(userId);
        } else {
            productRecommendationRepository.deleteBySessionId(sessionId);
        }

        List<Long> purchasedProductIds = userId == null ? List.of() : findPurchasedProductIds(userId);
        List<String> purchasedCategories = userId == null ? List.of() : findPurchasedCategories(userId);

        LinkedHashSet<Long> recommendedProductIds = new LinkedHashSet<>();

        if (!purchasedCategories.isEmpty()) {
            recommendedProductIds.addAll(findProductsByPurchaseHistory(purchasedCategories, purchasedProductIds, RECOMMENDATION_LIMIT));
        }

        if (recommendedProductIds.size() < RECOMMENDATION_LIMIT) {
            recommendedProductIds.addAll(findPopularProducts(purchasedProductIds, RECOMMENDATION_LIMIT - recommendedProductIds.size()));
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

            String reason = purchasedCategories.isEmpty() ? "popular" : "purchase_history";
            BigDecimal score = calculateScore(rank, reason);

            recommendations.add(new ProductRecommendation(
                    userId,
                    sessionId,
                    product,
                    reason,
                    score,
                    rank,
                    expiresAt
            ));

            rank++;
        }

        List<ProductRecommendation> savedRecommendations = productRecommendationRepository.saveAll(recommendations);

        return savedRecommendations.stream()
                .map(ProductRecommendationDTO::fromEntity)
                .toList();
    }

    @Override
    public List<ProductRecommendationDTO> getRecommendations(Long userId, String sessionId) {
        LocalDateTime now = LocalDateTime.now();

        if (userId != null) {
            return productRecommendationRepository
                    .findByUserIdAndExpiresAtAfterOrderByRankOrderAsc(userId, now)
                    .stream()
                    .map(ProductRecommendationDTO::fromEntity)
                    .toList();
        }

        if (sessionId != null && !sessionId.isBlank()) {
            return productRecommendationRepository
                    .findBySessionIdAndExpiresAtAfterOrderByRankOrderAsc(sessionId, now)
                    .stream()
                    .map(ProductRecommendationDTO::fromEntity)
                    .toList();
        }

        return List.of();
    }

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

    private List<String> findPurchasedCategories(Long userId) {
        String sql = """
                select p.category
                from orders o
                join order_items oi on o.id = oi.order_id
                join products p on oi.product_id = p.id
                where o.buyer_id = :userId
                  and o.status in ('delivered', 'confirmed')
                group by p.category
                order by count(*) desc
                limit 3
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        return namedParameterJdbcTemplate.queryForList(sql, params, String.class);
    }

    private List<Long> findProductsByPurchaseHistory(List<String> categories, List<Long> purchasedProductIds, int limit) {
        if (categories.isEmpty() || limit <= 0) {
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

    private List<Long> findPopularProducts(List<Long> purchasedProductIds, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        String sql = """
                select p.id
                from products p
                left join sales_rankings sr on sr.product_id = p.id
                where p.id not in (:purchasedProductIds)
                group by p.id
                order by coalesce(min(sr.rank), 999999), p.created_at desc
                limit :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("purchasedProductIds", normalizeIds(purchasedProductIds))
                .addValue("limit", limit);

        return namedParameterJdbcTemplate.queryForList(sql, params, Long.class);
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of(-1L);
        }

        return ids;
    }

    private BigDecimal calculateScore(int rank, String reason) {
        double baseScore = "purchase_history".equals(reason) ? 1.0 : 0.75;
        double score = Math.max(0.1, baseScore - ((rank - 1) * 0.05));

        return BigDecimal.valueOf(score)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private void validateRequest(ProductRecommendationDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("추천 요청 정보가 필요합니다.");
        }

        if (request.getUserId() == null && (request.getSessionId() == null || request.getSessionId().isBlank())) {
            throw new IllegalArgumentException("사용자 ID 또는 세션 ID가 필요합니다.");
        }
    }
}