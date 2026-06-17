package com.example.potatochip.product.entity;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.product.dto.ProductDTO;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "images")
public class Product {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id; //상품 ID

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "seller_id", nullable = false)
        private User seller; //판매자 ID

        @Column(nullable = false, length = 100)
        private String category; //카테고리

        @Column(nullable = false, length = 200)
        private String name; //농작물 이름

        private String description; //설명

        @Column(nullable = false)
        private BigDecimal price; //가격


        private BigDecimal discountPrice; //할인 가격


        private LocalDate discountStartAt; //할인 시작 일시


        private LocalDate discountEndAt; // 할인 종료 일시


        private String thumbnailUrl; //대표 사진

        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ProductImage> images = new ArrayList<>(); //상품 이미지 테이블과 연관관계 Casecade로 함께 삭제

        @Column(nullable = false)
        private String origin; // 원산지

        @Column(nullable = false)
        private LocalDate expiryDate; // 유통기한

        @Column(nullable = false)
        private Boolean isPickupAvailable; //픽업 가능 여부

        @Column(nullable = false)
        private Integer stockQuantity; // 재고 수량

        @Column(nullable = false)
        private LocalDateTime createdAt; //생성 일시

        @Column(nullable = false)
        private LocalDateTime updatedAt; //업데이트 일시

        private String address; // 주소


        private BigDecimal latitude; // 위도 (카카오맵)


        private BigDecimal longitude; // 경도 (카카오맵)


        private String operatingHours; //운영 시간

        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
        private List<Wishlist> wishlists = new ArrayList<>();



        public void changeEntity(ProductDTO productDTO) {

                this.category = productDTO.getCategory();
                this.name = productDTO.getName();
                this.description = productDTO.getDescription();
                this.price = productDTO.getPrice();
                this.discountPrice = productDTO.getDiscountPrice();
                this.discountStartAt = productDTO.getDiscountStartAt();
                this.discountEndAt = productDTO.getDiscountEndAt();
                this.thumbnailUrl = productDTO.getThumbnailUrl();
                this.origin = productDTO.getOrigin();
                this.expiryDate = productDTO.getExpiryDate();
                this.isPickupAvailable = productDTO.getIsPickupAvailable();
                this.stockQuantity = productDTO.getStockQuantity();
                this.address = productDTO.getAddress();
                this.latitude = productDTO.getLatitude();
                this.longitude = productDTO.getLongitude();
                this.operatingHours = productDTO.getOperatingHours();
                this.updatedAt = LocalDateTime.now();
        }

        @PrePersist
        public void prePersist() {
                this.createdAt = LocalDateTime.now();
                this.updatedAt = LocalDateTime.now();
        }



}