package com.example.potatochip.ai.claude;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class ClaudeClient {

    private static final String REVIEW_SUMMARY_SYSTEM_PROMPT = """
            너는 농산물 쇼핑몰의 상품 리뷰 요약기다.

            반드시 지켜라:
            - 반드시 한국어만 사용한다.
            - 한국어 이외의 언어를 사용하지 않는다.
            - 쇼핑몰 전체가 아니라 해당 상품에 대한 평가만 요약한다.
            - 추측 표현을 쓰지 않는다.
            - "보인다", "것 같다", "제공한다" 같은 표현을 쓰지 않는다.
            - 따옴표, 번호, 목록, 마크다운을 쓰지 않는다.
            - 최종 답변 문장만 출력한다.
            """;

    private static final String PRODUCT_RECOMMENDATION_SYSTEM_PROMPT = """
            너는 못난이 농산물 쇼핑몰의 상품 추천 문구 생성기다.

            반드시 지켜라:
            - 한국어만 사용한다.
            - 영어, 중국어, 일본어를 절대 사용하지 않는다.
            - 추천 이유 한 문장만 출력한다.
            - 따옴표, 번호, 목록, 마크다운을 쓰지 않는다.
            - 60자 이내로 작성한다.
            - 과장하지 않는다.
            - 없는 효능이나 품질을 지어내지 않는다.
            - “것 같다”, “보인다”, “추측된다”를 쓰지 않는다.
            - 출력은 추천 이유 문장 하나뿐이다.
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String anthropicVersion;

    public ClaudeClient(
            @Value("${anthropic.base-url:https://api.anthropic.com}") String baseUrl,
            @Value("${anthropic.api-key:}") String apiKey,
            @Value("${anthropic.model:claude-haiku-4-5}") String model,
            @Value("${anthropic.version:2023-06-01}") String anthropicVersion
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
        this.model = model;
        this.anthropicVersion = anthropicVersion;
    }

    public String chat(String prompt) {
        validateApiKey();

        Map<String, Object> request = Map.of(
                "model", model,
                "max_tokens", 500,
                "system", REVIEW_SUMMARY_SYSTEM_PROMPT,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", safePrompt(prompt)
                        )
                )
        );

        Map<?, ?> response = restClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", anthropicVersion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        String content = extractText(response);

        if (content.isBlank()) {
            throw new IllegalStateException("Claude content 응답이 비어 있습니다.");
        }

        return content.trim();
    }

    public String recommendProductReason(String prompt) {
        try {
            validateApiKey();

            Map<String, Object> request = Map.of(
                    "model", model,
                    "max_tokens", 120,
                    "system", PRODUCT_RECOMMENDATION_SYSTEM_PROMPT,
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", safePrompt(prompt)
                            )
                    )
            );

            Map<?, ?> response = restClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", anthropicVersion)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            String content = extractText(response);

            if (content.isBlank()) {
                return "구매 이력을 바탕으로 추천된 상품이에요.";
            }

            return content.trim();
        } catch (RuntimeException e) {
            return "구매 이력을 바탕으로 추천된 상품이에요.";
        }
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("anthropic.api-key 또는 ANTHROPIC_API_KEY 설정이 필요합니다.");
        }
    }

    private String safePrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "입력 내용이 없습니다.";
        }

        return prompt;
    }

    private String extractText(Map<?, ?> response) {
        if (response == null) {
            throw new IllegalStateException("Claude 응답이 비어 있습니다.");
        }

        Object contentObject = response.get("content");

        if (!(contentObject instanceof List<?> contentList) || contentList.isEmpty()) {
            throw new IllegalStateException("Claude content 응답 형식이 올바르지 않습니다.");
        }

        StringBuilder result = new StringBuilder();

        for (Object item : contentList) {
            if (!(item instanceof Map<?, ?> contentBlock)) {
                continue;
            }

            Object text = contentBlock.get("text");

            if (text != null) {
                result.append(text);
            }
        }

        return result.toString().trim();
    }
}