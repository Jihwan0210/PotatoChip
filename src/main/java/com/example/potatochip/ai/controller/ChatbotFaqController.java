package com.example.potatochip.ai.controller;

import com.example.potatochip.ai.dto.ChatbotFaqDTO;
import com.example.potatochip.ai.service.ChatbotFaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatbotFaqController {

    private final ChatbotFaqService chatbotFaqService;

    @GetMapping("/faq")
    public String faqPage() {
        return "faq/faq";
    }

    @GetMapping("/api/ai/chatbot/faqs")
    public ResponseEntity<List<ChatbotFaqDTO>> getFaqs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        List<ChatbotFaqDTO> faqs = chatbotFaqService.getFaqs(category, keyword);
        return ResponseEntity.ok(faqs);
    }

    @GetMapping("/api/ai/chatbot/faqs/{faqId}")
    public ResponseEntity<?> getFaqById(
            @PathVariable Long faqId
    ) {
        try {
            ChatbotFaqDTO faq = chatbotFaqService.getFaqById(faqId);
            return ResponseEntity.ok(faq);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/api/ai/chatbot/faqs")
    public ResponseEntity<?> createFaq(
            @RequestBody ChatbotFaqDTO chatbotFaqDTO
    ) {
        try {
            ChatbotFaqDTO faq = chatbotFaqService.createFaq(chatbotFaqDTO);
            return ResponseEntity.ok(faq);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PutMapping("/api/ai/chatbot/faqs/{faqId}")
    public ResponseEntity<?> updateFaq(
            @PathVariable Long faqId,
            @RequestBody ChatbotFaqDTO chatbotFaqDTO
    ) {
        try {
            ChatbotFaqDTO faq = chatbotFaqService.updateFaq(faqId, chatbotFaqDTO);
            return ResponseEntity.ok(faq);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @DeleteMapping("/api/ai/chatbot/faqs/{faqId}")
    public ResponseEntity<?> deleteFaq(
            @PathVariable Long faqId
    ) {
        try {
            chatbotFaqService.deleteFaq(faqId);
            return ResponseEntity.ok(
                    Map.of("message", "FAQ가 삭제되었습니다.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}