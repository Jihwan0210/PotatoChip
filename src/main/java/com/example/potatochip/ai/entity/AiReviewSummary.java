package com.example.potatochip.ai.entity;

import com.example.potatochip.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_review_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "product")
public class AiReviewSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AiReviewSummary(Product product, String summary, Integer reviewCount) {
        this.product = product;
        this.summary = summary;
        this.reviewCount = reviewCount;
        this.status = "completed";
        this.isActive = true;
        this.errorMessage = null;
        this.generatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getProductId() {
        return product == null ? null : product.getId();
    }

    public void updateSummary(String summary, Integer reviewCount) {
        this.summary = summary;
        this.reviewCount = reviewCount;
        this.status = "completed";
        this.isActive = true;
        this.errorMessage = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = "failed";
        this.isActive = false;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = "pending";
        }

        if (this.isActive == null) {
            this.isActive = false;
        }

        this.generatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}