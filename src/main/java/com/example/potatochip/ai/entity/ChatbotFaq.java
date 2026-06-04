package com.example.potatochip.ai.entity;

import com.example.potatochip.ai.dto.ChatbotFaqDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_faqs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatbotFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, length = 500)
    private String question;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(length = 500)
    private String keywords;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void changeEntity(ChatbotFaqDTO chatbotFaqDTO) {
        this.category = chatbotFaqDTO.getCategory();
        this.question = chatbotFaqDTO.getQuestion();
        this.answer = chatbotFaqDTO.getAnswer();
        this.keywords = chatbotFaqDTO.getKeywords();
        this.updatedBy = chatbotFaqDTO.getUpdatedBy();
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate(Long deletedBy) {
        this.isActive = false;
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.isActive == null) {
            this.isActive = true;
        }

        if (this.createdBy == null) {
            this.createdBy = 1L;
        }

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}