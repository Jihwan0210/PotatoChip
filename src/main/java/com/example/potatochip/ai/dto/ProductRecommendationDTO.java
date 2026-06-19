package com.example.potatochip.ai.dto;

import com.example.potatochip.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductRecommendationDTO {

    private Long productId;
    private String productName;
    private String productCategory;
    private String productOrigin;
    private String thumbnailUrl;
    private BigDecimal productPrice;
    private BigDecimal productDiscountPrice;
    private String reason;

    public static ProductRecommendationDTO from(Product product, String reason) {
        return new ProductRecommendationDTO(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getOrigin(),
                product.getThumbnailUrl(),
                product.getPrice(),
                product.getDiscountPrice(),
                reason
        );
    }
}
