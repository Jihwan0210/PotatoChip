package com.example.potatochip.ai.entity;

import com.example.potatochip.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_review_summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiReviewSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_review_summary_id")
    private Long aiReviewSummaryId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Lob
    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AiReviewSummary(Product product, String summary, Integer reviewCount) {
        this.product = product;
        this.summary = summary;
        this.reviewCount = reviewCount;
        this.isActive = true;
        this.generatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getProductId() {
        return product.getId();
    }

    public void updateSummary(String summary, Integer reviewCount) {
        this.summary = summary;
        this.reviewCount = reviewCount;
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
}