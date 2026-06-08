package com.example.potatochip.ai.repository;

import com.example.potatochip.ai.entity.ChatbotFaq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatbotFaqRepository extends JpaRepository<ChatbotFaq, Long> {

    List<ChatbotFaq> findByIsActiveTrueOrderByCreatedAtDesc();

    List<ChatbotFaq> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(String category);

    List<ChatbotFaq> findByQuestionContainingOrAnswerContainingOrKeywordsContainingOrderByCreatedAtDesc(
            String question,
            String answer,
            String keywords
    );

    List<ChatbotFaq> findTop5ByIsActiveTrueOrderByCreatedAtDesc();
}