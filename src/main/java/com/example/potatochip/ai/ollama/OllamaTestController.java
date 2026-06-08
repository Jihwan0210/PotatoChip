package com.example.potatochip.ai.ollama;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
@RequiredArgsConstructor
public class OllamaTestController {

    private final OllamaClient ollamaClient;

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> test(
            @RequestBody Map<String, String> request
    ) {
        String prompt = request.getOrDefault("prompt", "못난이 농산물 리뷰를 한 문장으로 요약해줘.");
        String response = ollamaClient.chat(prompt);

        return ResponseEntity.ok(
                Map.of("response", response)
        );
    }
}