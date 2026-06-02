package com.example.potatochip.ai.service;

import com.example.potatochip.ai.dto.ChatbotFaqDTO;
import com.example.potatochip.ai.entity.ChatbotFaq;
import com.example.potatochip.ai.repository.ChatbotFaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotFaqServiceImpl implements ChatbotFaqService {

    private final ChatbotFaqRepository chatbotFaqRepository;

    @Override
    public List<ChatbotFaqDTO> getFaqs(String category, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return chatbotFaqRepository
                    .findByQuestionContainingOrAnswerContainingOrKeywordsContainingOrderByCreatedAtDesc(
                            keyword,
                            keyword,
                            keyword
                    )
                    .stream()
                    .filter(ChatbotFaq::getIsActive)
                    .map(ChatbotFaqDTO::fromEntity)
                    .toList();
        }

        if (category != null && !category.isBlank() && !"all".equals(category)) {
            return chatbotFaqRepository
                    .findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(category)
                    .stream()
                    .map(ChatbotFaqDTO::fromEntity)
                    .toList();
        }

        return chatbotFaqRepository
                .findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(ChatbotFaqDTO::fromEntity)
                .toList();
    }

    @Override
    public ChatbotFaqDTO getFaqById(Long faqId) {
        ChatbotFaq chatbotFaq = chatbotFaqRepository.findById(faqId)
                .filter(ChatbotFaq::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("FAQ를 찾을 수 없습니다."));

        return ChatbotFaqDTO.fromEntity(chatbotFaq);
    }

    @Override
    @Transactional
    public ChatbotFaqDTO createFaq(ChatbotFaqDTO chatbotFaqDTO) {
        validateFaq(chatbotFaqDTO);

        ChatbotFaq chatbotFaq = new ChatbotFaq();
        chatbotFaq.setCategory(chatbotFaqDTO.getCategory());
        chatbotFaq.setQuestion(chatbotFaqDTO.getQuestion());
        chatbotFaq.setAnswer(chatbotFaqDTO.getAnswer());
        chatbotFaq.setKeywords(chatbotFaqDTO.getKeywords());
        chatbotFaq.setCreatedBy(chatbotFaqDTO.getCreatedBy() == null ? 1L : chatbotFaqDTO.getCreatedBy());

        ChatbotFaq savedFaq = chatbotFaqRepository.save(chatbotFaq);

        return ChatbotFaqDTO.fromEntity(savedFaq);
    }

    @Override
    @Transactional
    public ChatbotFaqDTO updateFaq(Long faqId, ChatbotFaqDTO chatbotFaqDTO) {
        validateFaq(chatbotFaqDTO);

        ChatbotFaq chatbotFaq = chatbotFaqRepository.findById(faqId)
                .filter(ChatbotFaq::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("수정할 FAQ를 찾을 수 없습니다."));

        if (chatbotFaqDTO.getUpdatedBy() == null) {
            chatbotFaqDTO.setUpdatedBy(1L);
        }

        chatbotFaq.changeEntity(chatbotFaqDTO);

        return ChatbotFaqDTO.fromEntity(chatbotFaq);
    }

    @Override
    @Transactional
    public void deleteFaq(Long faqId) {
        ChatbotFaq chatbotFaq = chatbotFaqRepository.findById(faqId)
                .filter(ChatbotFaq::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 FAQ를 찾을 수 없습니다."));

        chatbotFaq.deactivate(1L);
    }

    private void validateFaq(ChatbotFaqDTO chatbotFaqDTO) {
        if (chatbotFaqDTO.getCategory() == null || chatbotFaqDTO.getCategory().isBlank()) {
            throw new IllegalArgumentException("FAQ 카테고리를 입력해주세요.");
        }

        if (chatbotFaqDTO.getQuestion() == null || chatbotFaqDTO.getQuestion().isBlank()) {
            throw new IllegalArgumentException("FAQ 질문을 입력해주세요.");
        }

        if (chatbotFaqDTO.getAnswer() == null || chatbotFaqDTO.getAnswer().isBlank()) {
            throw new IllegalArgumentException("FAQ 답변을 입력해주세요.");
        }
    }
}