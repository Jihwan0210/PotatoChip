package com.example.potatochip.ai.repository;

import com.example.potatochip.ai.entity.ChatbotLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatbotLogRepository extends JpaRepository<ChatbotLog, Long> {

    List<ChatbotLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ChatbotLog> findAllByOrderByCreatedAtDesc();

    List<ChatbotLog> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}