package com.example.potatochip.product.service.ai;

import com.example.potatochip.product.dto.ai.AnalyzeImageResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProductImageAnalysisServiceImpl implements ProductImageAnalysisService {

    private final ChatModel chatModel;

    @Override
    public AnalyzeImageResult analyze(MultipartFile image) throws IOException {
        MimeType mimeType = "image/png".equals(image.getContentType())
                ? MimeTypeUtils.IMAGE_PNG
                : MimeTypeUtils.IMAGE_JPEG;
        Resource imageResource = new ByteArrayResource(image.getBytes());

        String prompt = """
                이 농산물 사진을 보고 온라인 마켓에 등록할 상품명과 상품 설명을 한국어로 만들어줘.
                반드시 아래 JSON 형식으로만 답해. 다른 말은 절대 하지 마.
                {"name": "상품명", "description": "2~3문장 설명"}
                """;

        UserMessage userMessage = UserMessage.builder()
                .text(prompt)
                .media(new Media(mimeType, imageResource))
                .build();

        ChatResponse response = chatModel.call(new Prompt(userMessage,
                ChatOptions.builder().model(String.valueOf(OllamaModel.LLAVA)).build()));

        String raw = response.getResult().getOutput().getText();
        String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(cleaned);
            return new AnalyzeImageResult(node.path("name").asText(""), node.path("description").asText(""));
        } catch (Exception e) {
            return new AnalyzeImageResult("", cleaned);
        }
    }
}