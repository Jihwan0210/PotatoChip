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
    @Column(name = "chatbot_faq_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 300)
    private String question;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(length = 300)
    private String keywords;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void changeEntity(ChatbotFaqDTO chatbotFaqDTO) {
        this.category = chatbotFaqDTO.getCategory();
        this.question = chatbotFaqDTO.getQuestion();
        this.answer = chatbotFaqDTO.getAnswer();
        this.keywords = chatbotFaqDTO.getKeywords();
        this.displayOrder = chatbotFaqDTO.getDisplayOrder();
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}