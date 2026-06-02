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
@ToString
public class ChatbotLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "is_answered", nullable = false)
    private Boolean isAnswered;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ChatbotLog(
            Long userId,
            String sessionId,
            String question,
            String answer,
            String sourceType,
            Long sourceId,
            Boolean isAnswered,
            String errorMessage
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.question = question;
        this.answer = answer;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.isAnswered = isAnswered;
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.sourceType == null) {
            this.sourceType = "fallback";
        }

        if (this.isAnswered == null) {
            this.isAnswered = true;
        }

        this.createdAt = LocalDateTime.now();
    }
}