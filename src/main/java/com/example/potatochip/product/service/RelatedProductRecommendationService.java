package com.example.potatochip.product.service;

import com.example.potatochip.product.dto.RelatedProductRecommendationDTO;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatedProductRecommendationService {

    private static final int RELATED_LIMIT = 3;

    private final ProductRepository productRepository;

    public List<RelatedProductRecommendationDTO> getProducts(Long productId) {
        Product baseProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        Map<Long, RelatedProductRecommendationDTO> result = new LinkedHashMap<>();

        List<Product> sameCategoryProducts = productRepository.findRelatedByCategory(
                baseProduct.getCategory(),
                baseProduct.getId(),
                PageRequest.of(0, RELATED_LIMIT)
        );

        for (Product product : sameCategoryProducts) {
            addRecommendation(result, product, "같은 " + baseProduct.getCategory() + " 카테고리 상품이에요.");
        }

        if (result.size() < RELATED_LIMIT && baseProduct.getSeller() != null) {
            List<Product> sameSellerProducts = productRepository.findRelatedBySeller(
                    baseProduct.getSeller().getId(),
                    baseProduct.getId(),
                    PageRequest.of(0, RELATED_LIMIT)
            );

            for (Product product : sameSellerProducts) {
                addRecommendation(result, product, "같은 판매자의 다른 상품이에요.");

                if (result.size() >= RELATED_LIMIT) {
                    break;
                }
            }
        }

        return result.values().stream().limit(RELATED_LIMIT).toList();
    }

    private void addRecommendation(
            Map<Long, RelatedProductRecommendationDTO> result,
            Product product,
            String reason
    ) {
        if (product == null || result.containsKey(product.getId())) {
            return;
        }

        result.put(product.getId(), RelatedProductRecommendationDTO.from(product, reason));
    }
}
