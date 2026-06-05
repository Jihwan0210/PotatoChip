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

    private LocalDate discountStartAt; //할인 시작날짜

    private LocalDate discountEndAt; //할인 종료날짜

    private String thumbnailUrl;

    private String origin;

    private LocalDate expiryDate;

    private Boolean isPickupAvailable;

    private Integer stockQuantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String address; // 주소

    private BigDecimal latitude; // 위도 (카카오맵)

    private BigDecimal longitude; // 경도 (카카오맵)

    private String operatingHours; //운영 시간

}
