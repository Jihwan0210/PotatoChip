package com.example.potatochip.admin.dto;

import com.example.potatochip.product.entity.Product;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductDTO {

    private Long id;
    private Long sellerId;
    private String sellerName;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private String origin;
    private LocalDate expiryDate;
    private Boolean pickupAvailable;
    private LocalDateTime createdAt;
    private String statusText;

    public static AdminProductDTO fromEntity(Product product) {
        return AdminProductDTO.builder()
                .id(product.getId())
                .sellerId(product.getSeller() == null ? null : product.getSeller().getId())
                .sellerName(product.getSeller() == null ? null : product.getSeller().getName())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .stockQuantity(product.getStockQuantity())
                .origin(product.getOrigin())
                .expiryDate(product.getExpiryDate())
                .pickupAvailable(product.getIsPickupAvailable())
                .createdAt(product.getCreatedAt())
                .statusText(getStatusText(product))
                .build();
    }

    private static String getStatusText(Product product) {
        if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
            return "품절";
        }

        if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(LocalDate.now())) {
            return "판매 종료";
        }

        return "판매중";
    }
}