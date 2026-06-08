package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.ChatbotFaqDTO;

import java.util.List;

public interface ChatbotFaqService {

    List<ChatbotFaqDTO> getFaqs(String category, String keyword);

    ChatbotFaqDTO getFaqById(Long faqId);

    ChatbotFaqDTO createFaq(ChatbotFaqDTO chatbotFaqDTO);

    ChatbotFaqDTO updateFaq(Long faqId, ChatbotFaqDTO chatbotFaqDTO);

    void deleteFaq(Long faqId);
}