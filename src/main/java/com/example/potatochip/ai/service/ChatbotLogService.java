package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.ChatbotDTO;

import java.util.List;

public interface ChatbotLogService {

    ChatbotDTO ask(ChatbotDTO chatbotDTO);

    List<ChatbotDTO> getChatbotLogs(Long userId);
}