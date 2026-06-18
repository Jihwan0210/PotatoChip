package com.example.potatochip.ai.entity;

import com.example.potatochip.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "product")
public class ProductRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductRecommendationType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    public ProductRecommendation(Long userId, String sessionId, Product product, ProductRecommendationType type, String reason, BigDecimal score, Integer rankOrder, LocalDateTime expiresAt) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.product = product;
        this.type = type;
        this.reason = reason;
        this.score = score;
        this.rankOrder = rankOrder;
        this.expiresAt = expiresAt;
        this.generatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.generatedAt == null) {
            this.generatedAt = LocalDateTime.now();
        }

        if (this.type == null) {
            this.type = ProductRecommendationType.AI;
        }
    }


}