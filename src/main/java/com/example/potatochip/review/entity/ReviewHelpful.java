package com.example.potatochip.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "review_helpfuls",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_helpful_review_user",
                        columnNames = {"review_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewHelpful {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_helpful_id")
    private Long reviewHelpfulId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ReviewHelpful(Review review, Long userId) {
        this.review = review;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getReviewId() {
        return review.getReviewId();
    }
}