package com.example.potatochip.ai.repository;

import com.example.potatochip.ai.entity.ChatbotFaq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatbotFaqRepository extends JpaRepository<ChatbotFaq, Long> {

    List<ChatbotFaq> findByIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc();

    List<ChatbotFaq> findByCategoryAndIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc(String category);

    List<ChatbotFaq> findByQuestionContainingOrAnswerContainingOrKeywordsContainingOrderByDisplayOrderAscCreatedAtDesc(
            String question,
            String answer,
            String keywords
    );
}