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
    private String sessionId;
    private String question;
    private String answer;
    private String sourceType;
    private Long sourceId;
    private Boolean isAnswered;
    private String errorMessage;
    private LocalDateTime createdAt;

    public static ChatbotDTO fromEntity(ChatbotLog chatbotLog) {
        return new ChatbotDTO(
                chatbotLog.getId(),
                chatbotLog.getUserId(),
                chatbotLog.getSessionId(),
                chatbotLog.getQuestion(),
                chatbotLog.getAnswer(),
                chatbotLog.getSourceType(),
                chatbotLog.getSourceId(),
                chatbotLog.getIsAnswered(),
                chatbotLog.getErrorMessage(),
                chatbotLog.getCreatedAt()
        );
    }
}