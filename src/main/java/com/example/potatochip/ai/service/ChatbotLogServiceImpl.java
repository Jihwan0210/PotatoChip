package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.ChatbotDTO;
import com.example.potatochip.ai.entity.ChatbotFaq;
import com.example.potatochip.ai.entity.ChatbotLog;
import com.example.potatochip.ai.repository.ChatbotFaqRepository;
import com.example.potatochip.ai.repository.ChatbotLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotLogServiceImpl implements ChatbotLogService {

    private final ChatbotFaqRepository chatbotFaqRepository;
    private final ChatbotLogRepository chatbotLogRepository;

    @Override
    @Transactional
    public ChatbotDTO ask(ChatbotDTO chatbotDTO) {
        validateQuestion(chatbotDTO);

        String question = chatbotDTO.getQuestion().trim();
        List<ChatbotFaq> faqs = chatbotFaqRepository.findByIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc();

        ChatbotFaq matchedFaq = findBestMatchedFaq(question, faqs);
        String answer;
        String sourceType;

        if (matchedFaq != null) {
            answer = matchedFaq.getAnswer();
            sourceType = "FAQ";
        } else {
            answer = "아직 해당 문의에 맞는 FAQ를 찾지 못했어요. 배송, 환불, 픽업, 상품 품질, AI 리뷰 총평처럼 구체적인 키워드로 다시 질문해보거나 문의 게시판을 이용해주세요.";
            sourceType = "FALLBACK";
        }

        ChatbotLog chatbotLog = new ChatbotLog(
                chatbotDTO.getUserId(),
                matchedFaq,
                question,
                answer,
                sourceType
        );

        ChatbotLog savedLog = chatbotLogRepository.save(chatbotLog);

        return ChatbotDTO.fromEntity(savedLog);
    }

    @Override
    public List<ChatbotDTO> getChatbotLogs(Long userId) {
        List<ChatbotLog> logs;

        if (userId != null) {
            logs = chatbotLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else {
            logs = chatbotLogRepository.findAllByOrderByCreatedAtDesc();
        }

        return logs.stream()
                .map(ChatbotDTO::fromEntity)
                .toList();
    }

    private ChatbotFaq findBestMatchedFaq(String question, List<ChatbotFaq> faqs) {
        String normalizedQuestion = normalize(question);

        return faqs.stream()
                .map(faq -> new FaqMatchResult(faq, calculateScore(normalizedQuestion, faq)))
                .filter(result -> result.score() > 0)
                .max(Comparator.comparingInt(FaqMatchResult::score))
                .map(FaqMatchResult::faq)
                .orElse(null);
    }

    private int calculateScore(String question, ChatbotFaq faq) {
        int score = 0;

        String faqQuestion = normalize(faq.getQuestion());
        String faqAnswer = normalize(faq.getAnswer());
        String faqKeywords = normalize(faq.getKeywords());

        if (!faqQuestion.isBlank() && question.contains(faqQuestion)) {
            score += 10;
        }

        if (!faqQuestion.isBlank() && faqQuestion.contains(question)) {
            score += 8;
        }

        if (!faqKeywords.isBlank()) {
            String[] keywords = faqKeywords.split(",");

            for (String keyword : keywords) {
                String normalizedKeyword = normalize(keyword);

                if (!normalizedKeyword.isBlank() && question.contains(normalizedKeyword)) {
                    score += 5;
                }
            }
        }

        if (!faqQuestion.isBlank()) {
            String[] words = faqQuestion.split(" ");

            for (String word : words) {
                String normalizedWord = normalize(word);

                if (normalizedWord.length() >= 2 && question.contains(normalizedWord)) {
                    score += 2;
                }
            }
        }

        if (!faqAnswer.isBlank()) {
            String[] words = faqAnswer.split(" ");

            for (String word : words) {
                String normalizedWord = normalize(word);

                if (normalizedWord.length() >= 2 && question.contains(normalizedWord)) {
                    score += 1;
                }
            }
        }

        return score;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase()
                .replace("?", "")
                .replace("!", "")
                .replace(".", "")
                .replace(",", "")
                .trim();
    }

    private void validateQuestion(ChatbotDTO chatbotDTO) {
        if (chatbotDTO == null || chatbotDTO.getQuestion() == null || chatbotDTO.getQuestion().isBlank()) {
            throw new IllegalArgumentException("문의 내용을 입력해주세요.");
        }
    }

    private record FaqMatchResult(ChatbotFaq faq, int score) {
    }
}