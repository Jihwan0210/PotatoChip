package com.example.potatochip.ai.ollama;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {

    private final RestClient restClient;
    private final String model;

    public OllamaClient(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:gemma2:latest}") String model
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.model = model;
    }

    public String chat(String prompt) {
        Map<String, Object> request = Map.of(
                "model", model,
                "stream", false,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        너는 농산물 쇼핑몰의 상품 리뷰 요약기다.
                                        
                                                                   아래 리뷰 내용만 근거로 상품 리뷰 요약문을 작성해라.
                                        
                                                                   조건:
                                                                   - 반드시 한국어만 사용한다.
                                                                   - 영어 단어를 사용하지 않는다.
                                                                   - 쇼핑몰 전체가 아니라 해당 상품에 대한 평가만 요약한다.
                                                                   - 추측 표현을 쓰지 않는다.
                                                                   - "보인다", "것 같다", "제공한다" 같은 표현을 쓰지 않는다.
                                                                   - 한 문장으로 작성한다.
                                                                   - 60자 이내로 작성한다.
                                        
                                                                   리뷰:
                                                                   감자가 신선하고 맛있어요.
                                                                   가격이 저렴해서 만족합니다.
                                                                   배송이 빨랐어요.
                                        
                                                                   좋은 예시:
                                                                   감자는 신선하고 맛이 좋으며 가격과 배송 만족도가 높습니다.
                                        
                                                                   요약:
                                        """
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "options", Map.of(
                        "temperature", 0.2,
                        "top_p", 0.8
                )
        );

        Map response = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("message") == null) {
            throw new IllegalStateException("Ollama 응답이 비어 있습니다.");
        }

        Object messageObject = response.get("message");

        if (!(messageObject instanceof Map<?, ?> message)) {
            throw new IllegalStateException("Ollama message 응답 형식이 올바르지 않습니다.");
        }

        Object content = message.get("content");

        if (content == null) {
            throw new IllegalStateException("Ollama content 응답이 비어 있습니다.");
        }

        return content.toString().trim();
    }
}