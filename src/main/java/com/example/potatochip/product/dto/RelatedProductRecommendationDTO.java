package com.example.potatochip.product.dto;

import com.example.potatochip.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RelatedProductRecommendationDTO {

    private Long productId;
    private String productName;
    private String productCategory;
    private String productOrigin;
    private String thumbnailUrl;
    private BigDecimal productPrice;
    private BigDecimal productDiscountPrice;
    private String reason;

    public static RelatedProductRecommendationDTO from(Product product, String reason) {
        return new RelatedProductRecommendationDTO(
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
