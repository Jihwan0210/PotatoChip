package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.ChatbotDTO;
import com.example.potatochip.ai.entity.ChatbotFaq;
import com.example.potatochip.ai.entity.ChatbotLog;
import com.example.potatochip.ai.repository.ChatbotFaqRepository;
import com.example.potatochip.ai.repository.ChatbotLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotLogServiceImpl implements ChatbotLogService {

    private static final int CURRENT_QUESTION_MATCH_SCORE = 5;

    private final ChatbotFaqRepository chatbotFaqRepository;
    private final ChatbotLogRepository chatbotLogRepository;

    @Override
    @Transactional
    public ChatbotDTO ask(ChatbotDTO chatbotDTO) {
        validateQuestion(chatbotDTO);

        String question = chatbotDTO.getQuestion().trim();

        List<ChatbotFaq> faqs = chatbotFaqRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        List<ChatbotLog> recentLogs = findRecentLogs(chatbotDTO.getUserId(), chatbotDTO.getSessionId());

        ChatbotFaq matchedFaq = findBestMatchedFaq(question, recentLogs, faqs);

        String answer;
        String sourceType;
        Long sourceId;
        Boolean isAnswered;
        String errorMessage;

        if (matchedFaq != null) {
            answer = matchedFaq.getAnswer();
            sourceType = "faq";
            sourceId = matchedFaq.getId();
            isAnswered = true;
            errorMessage = null;
        } else {
            answer = "아직 해당 문의에 맞는 FAQ를 찾지 못했어요. 배송, 환불, 픽업, 상품 품질, AI 리뷰 총평처럼 구체적인 키워드로 다시 질문해보거나 문의 게시판을 이용해주세요.";
            sourceType = "fallback";
            sourceId = null;
            isAnswered = false;
            errorMessage = "FAQ 매칭 실패";
        }

        ChatbotLog chatbotLog = new ChatbotLog(
                chatbotDTO.getUserId(),
                chatbotDTO.getSessionId(),
                question,
                answer,
                sourceType,
                sourceId,
                isAnswered,
                errorMessage
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

    private ChatbotFaq findBestMatchedFaq(String question, List<ChatbotLog> recentLogs, List<ChatbotFaq> faqs) {
        FaqMatchResult currentMatch = findBestCurrentQuestionMatch(question, faqs);

        if (currentMatch != null && currentMatch.score() >= CURRENT_QUESTION_MATCH_SCORE) {
            return currentMatch.faq();
        }

        if (isFollowUpQuestion(question)) {
            ChatbotFaq recentMatchedFaq = findRecentMatchedFaq(recentLogs);

            if (recentMatchedFaq != null && Boolean.TRUE.equals(recentMatchedFaq.getIsActive())) {
                return recentMatchedFaq;
            }
        }

        if (currentMatch != null && currentMatch.score() > 0) {
            return currentMatch.faq();
        }

        return null;
    }

    private FaqMatchResult findBestCurrentQuestionMatch(String question, List<ChatbotFaq> faqs) {
        String normalizedQuestion = normalize(question);

        return faqs.stream()
                .map(faq -> new FaqMatchResult(faq, calculateCurrentQuestionScore(normalizedQuestion, faq)))
                .filter(result -> result.score() > 0)
                .max(Comparator.comparingInt(FaqMatchResult::score))
                .orElse(null);
    }

    private int calculateCurrentQuestionScore(String question, ChatbotFaq faq) {
        int score = 0;

        String normalizedQuestion = normalize(question);
        String faqQuestion = normalize(faq.getQuestion());
        String faqAnswer = normalize(faq.getAnswer());
        String faqKeywords = normalize(faq.getKeywords());
        String combinedFaqText = normalize(
                safeText(faq.getQuestion()) + " " +
                        safeText(faq.getAnswer()) + " " +
                        safeText(faq.getKeywords()) + " " +
                        getCategoryKoreanName(faq.getCategory())
        );

        score += calculateCategoryScore(normalizedQuestion, faq.getCategory());

        if (!faqQuestion.isBlank() && normalizedQuestion.contains(faqQuestion)) {
            score += 20;
        }

        if (!faqQuestion.isBlank() && faqQuestion.contains(normalizedQuestion)) {
            score += 15;
        }

        if (!faqKeywords.isBlank()) {
            String[] keywords = safeText(faq.getKeywords()).split(",");

            for (String keyword : keywords) {
                String normalizedKeyword = normalize(keyword);

                if (!normalizedKeyword.isBlank() && normalizedQuestion.contains(normalizedKeyword)) {
                    score += 10;
                }
            }
        }

        List<String> questionTokens = extractQuestionTokens(question);

        for (String token : questionTokens) {
            if (combinedFaqText.contains(token)) {
                score += 4;
            }

            if (faqQuestion.contains(token)) {
                score += 3;
            }

            if (faqAnswer.contains(token)) {
                score += 1;
            }
        }

        return score;
    }

    private int calculateCategoryScore(String question, String category) {
        if (category == null) {
            return 0;
        }

        return switch (category) {
            case "delivery" -> containsAny(question, "배송", "택배", "출고", "도착", "배달", "픽업", "수령", "며칠", "얼마나", "언제") ? 20 : 0;
            case "refund" -> containsAny(question, "환불", "교환", "취소", "상했", "상함", "불량", "파손", "반품", "계좌", "입금", "돈") ? 20 : 0;
            case "quality" -> containsAny(question, "품질", "흠집", "신선", "상태", "못난이", "먹어도", "괜찮") ? 20 : 0;
            case "ai" -> containsAny(question, "ai", "총평", "리뷰", "요약", "챗봇") ? 20 : 0;
            case "order" -> containsAny(question, "주문", "결제", "결제수단", "가격", "구매") ? 20 : 0;
            default -> 0;
        };
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }

        for (String keyword : keywords) {
            if (text.contains(normalize(keyword))) {
                return true;
            }
        }

        return false;
    }

    private List<String> extractQuestionTokens(String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }

        return Arrays.stream(question.split("\\s+"))
                .map(this::normalizeToken)
                .filter(token -> token.length() >= 2)
                .toList();
    }

    private String normalizeToken(String value) {
        return normalize(value)
                .replaceAll("(은|는|이|가|을|를|도|에|에서|으로|로|와|과|랑|하고)$", "");
    }

    private String getCategoryKoreanName(String category) {
        if (category == null) {
            return "";
        }

        return switch (category) {
            case "order" -> "주문 결제";
            case "delivery" -> "배송 픽업 택배 출고 도착 수령";
            case "refund" -> "교환 환불 반품 취소 계좌 입금";
            case "quality" -> "상품 품질 신선도 흠집 못난이";
            case "ai" -> "AI 리뷰 총평 요약 챗봇";
            default -> category;
        };
    }

    private ChatbotFaq findRecentMatchedFaq(List<ChatbotLog> recentLogs) {
        if (recentLogs == null || recentLogs.isEmpty()) {
            return null;
        }

        return recentLogs.stream()
                .filter(log -> "faq".equals(log.getSourceType()))
                .filter(log -> log.getSourceId() != null)
                .map(log -> chatbotFaqRepository.findById(log.getSourceId()).orElse(null))
                .filter(faq -> faq != null && Boolean.TRUE.equals(faq.getIsActive()))
                .findFirst()
                .orElse(null);
    }

    private List<ChatbotLog> findRecentLogs(Long userId, String sessionId) {
        if (userId != null) {
            return chatbotLogRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        }

        if (sessionId != null && !sessionId.isBlank()) {
            return chatbotLogRepository.findTop5BySessionIdOrderByCreatedAtDesc(sessionId);
        }

        return List.of();
    }

    private boolean isFollowUpQuestion(String question) {
        String normalizedQuestion = normalize(question);

        return normalizedQuestion.startsWith("그럼")
                || normalizedQuestion.startsWith("그러면")
                || normalizedQuestion.startsWith("그건")
                || normalizedQuestion.startsWith("그거")
                || normalizedQuestion.startsWith("그럼요")
                || normalizedQuestion.startsWith("그리고")
                || normalizedQuestion.startsWith("또")
                || normalizedQuestion.contains("토요일에도")
                || normalizedQuestion.contains("일요일에도")
                || normalizedQuestion.contains("주말에도")
                || normalizedQuestion.contains("그때")
                || normalizedQuestion.contains("이것도")
                || normalizedQuestion.contains("그것도")
                || normalizedQuestion.contains("가능해")
                || normalizedQuestion.contains("가능한가")
                || normalizedQuestion.contains("받을수")
                || normalizedQuestion.contains("받을 수")
                || normalizedQuestion.contains("들어오나요")
                || normalizedQuestion.contains("계좌")
                || normalizedQuestion.contains("입금");
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
                .replace("~", "")
                .replace(" ", "")
                .trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void validateQuestion(ChatbotDTO chatbotDTO) {
        if (chatbotDTO == null || chatbotDTO.getQuestion() == null || chatbotDTO.getQuestion().isBlank()) {
            throw new IllegalArgumentException("문의 내용을 입력해주세요.");
        }
    }

    private record FaqMatchResult(ChatbotFaq faq, int score) {
    }
}