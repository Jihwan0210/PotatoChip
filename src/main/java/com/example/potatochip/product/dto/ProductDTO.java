package com.example.potatochip.product.dto;

import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.entity.ProductImage;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductDTO {

    private Long id; //상품 ID

    private Long sellerId; //판매자 ID

    private String category; // 카테고리

    private String name; // 농작물 이름

    private String description; // 설명

    private BigDecimal price; //가격

    private BigDecimal discountPrice; //할인가격

    private LocalDate discountStartAt; //할인 시작날짜

    private LocalDate discountEndAt; //할인 종료날짜

    private String thumbnailUrl; //대표 사진

    private String origin; // 원산지

    private LocalDate expiryDate; //유통기한

    private Boolean isPickupAvailable; //픽업 가능 여부

    private Integer stockQuantity; //재고 수량

    private LocalDateTime createdAt; //생성 일시

    private LocalDateTime updatedAt; //업데이트 일시

    private String address; // 주소

    private BigDecimal latitude; // 위도 (카카오맵)

    private BigDecimal longitude; // 경도 (카카오맵)

    private String operatingHours; //운영 시간

    private List<ProductImage> images;

}
