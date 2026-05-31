package com.example.potatochip.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "matchedFaq")
public class ChatbotLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatbot_log_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_faq_id")
    private ChatbotFaq matchedFaq;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ChatbotLog(Long userId, ChatbotFaq matchedFaq, String question, String answer, String sourceType) {
        this.userId = userId;
        this.matchedFaq = matchedFaq;
        this.question = question;
        this.answer = answer;
        this.sourceType = sourceType;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}