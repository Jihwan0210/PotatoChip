package com.example.potatochip.ai.dto;

import com.example.potatochip.ai.entity.ChatbotLog;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatbotDTO {

    private Long id;
    private Long userId;
    private Long matchedFaqId;
    private String question;
    private String answer;
    private String sourceType;
    private LocalDateTime createdAt;

    public static ChatbotDTO fromEntity(ChatbotLog chatbotLog) {
        Long matchedFaqId = chatbotLog.getMatchedFaq() == null ? null : chatbotLog.getMatchedFaq().getId();

        return new ChatbotDTO(
                chatbotLog.getId(),
                chatbotLog.getUserId(),
                matchedFaqId,
                chatbotLog.getQuestion(),
                chatbotLog.getAnswer(),
                chatbotLog.getSourceType(),
                chatbotLog.getCreatedAt()
        );
    }
}