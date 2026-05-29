package com.example.potatochip.product.entity;

import com.example.potatochip.product.dto.ProductDTO;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "seller_id", nullable = false)
        private Long sellerId;

        @Column(nullable = false, length = 100)
        private String category;

        @Column(nullable = false, length = 200)
        private String name;

        private String description;

        @Column(nullable = false)
        private BigDecimal price;


        private BigDecimal discountPrice;


        private LocalDateTime discountStartAt;


        private LocalDateTime discountEndAt;


        private String thumbnailUrl;

        @Column(nullable = false)
        private String origin;

        @Column(nullable = false)
        private LocalDate expiryDate;

        @Column(nullable = false)
        private Boolean isPickupAvailable;

        @Column(nullable = false)
        private Integer stockQuantity;

        @Column(nullable = false)
        private LocalDateTime createdAt;

        @Column(nullable = false)
        private LocalDateTime updatedAt;

        public void changeEntity(ProductDTO productDTO) {

                this.sellerId = productDTO.getSellerId();
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
                this.updatedAt = LocalDateTime.now();
        }

        @PrePersist
        public void prePersist() {
                this.createdAt = LocalDateTime.now();
                this.updatedAt = LocalDateTime.now();
        }



}