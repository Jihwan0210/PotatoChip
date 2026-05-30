package com.example.potatochip.ai.dto;

import com.example.potatochip.ai.entity.ChatbotFaq;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatbotFaqDTO {

    private Long id;

    private String category;

    private String question;

    private String answer;

    private String keywords;

    private Integer displayOrder;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ChatbotFaqDTO fromEntity(ChatbotFaq chatbotFaq) {
        return new ChatbotFaqDTO(
                chatbotFaq.getId(),
                chatbotFaq.getCategory(),
                chatbotFaq.getQuestion(),
                chatbotFaq.getAnswer(),
                chatbotFaq.getKeywords(),
                chatbotFaq.getDisplayOrder(),
                chatbotFaq.getIsActive(),
                chatbotFaq.getCreatedAt(),
                chatbotFaq.getUpdatedAt()
        );
    }
}