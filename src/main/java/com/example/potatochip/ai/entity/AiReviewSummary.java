package com.example.potatochip.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_review_summaries")
public class AiReviewSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_review_summary_id")
    private Long aiReviewSummaryId;

    // TODO: Product Entity가 main 브랜치에 병합되면
    //  Long productId 대신 Product 연관관계로 변경 필요
    //
    //  변경 예정:
    //  @OneToOne(fetch = FetchType.LAZY)
    //  @JoinColumn(name = "product_id", nullable = false, unique = true)
    //  private Product product;
    //
    //  추가 예정 import:
    //  import com.example.potatochip.product.entity.Product;
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

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

    protected AiReviewSummary() {
    }

    // TODO: Product Entity 연동 후 생성자 파라미터 변경 필요
    //
    //  현재:
    //  AiReviewSummary(Long productId, String summary, Integer reviewCount)
    //
    //  변경 예정:
    //  AiReviewSummary(Product product, String summary, Integer reviewCount)
    public AiReviewSummary(Long productId, String summary, Integer reviewCount) {
        this.productId = productId;
        this.summary = summary;
        this.reviewCount = reviewCount;
        this.isActive = true;
        this.generatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getAiReviewSummaryId() {
        return aiReviewSummaryId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSummary() {
        return summary;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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