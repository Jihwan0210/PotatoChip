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
        이 사진에 있는 농산물을 보고 아래 JSON 형식으로만 답해. 다른 말은 절대 하지 마. 마크다운 금지.
        {"name": "상품명", "description": "상품설명"}
        
        규칙:
        - name: 농산물 이름과 중량 포함 (예: 못난이 감자 3kg)
        - description: 아래 말투와 규칙을 따를 것
          * 농부가 직접 손으로 쓴 것처럼 구어체로
          * 자기 농장 자랑, 직접 키웠다는 느낌
          * 못생겼지만 맛은 보장한다는 내용 자연스럽게 포함
          * 2~3문장, 너무 길지 않게
          * AI 느낌 나는 단어 금지 (최상급, 신선도, 영양, 친환경 같은 딱딱한 단어 쓰지 마)
        
        예시 description:
        "저희 밭에서 직접 캔 감자인데 모양이 좀 삐뚤어졌어요 ㅎㅎ 그래도 쪄먹으면 진짜 맛있습니다. 모양만 못났지 맛은 자신있어요!"
        "올해 사과가 좀 작고 울퉁불퉁하게 자랐는데 당도는 오히려 더 좋아요. 직접 먹어보고 올리는 거라 믿고 드셔도 됩니다~"
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