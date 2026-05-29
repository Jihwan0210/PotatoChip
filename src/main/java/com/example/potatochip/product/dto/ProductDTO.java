package com.example.potatochip.product.dto;

import com.example.potatochip.product.entity.Product;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductDTO {

    private Long id;

    private Long sellerId;

    private String category;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private LocalDateTime discountStartAt;

    private LocalDateTime discountEndAt;

    private String thumbnailUrl;
    private String origin;
    private LocalDate expiryDate;
    private Boolean isPickupAvailable;
    private Integer stockQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
