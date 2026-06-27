package com.example.potatochip.product.chat.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private Boolean isRead;
    private String sentAt;
    private String senderEmail;
}