package com.example.potatochip.inquiry.dto;

import com.example.potatochip.inquiry.entity.Inquiry;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InquiryDTO {

    private Long id;
    private Long userId;
    private Long productId;
    private Long orderId;
    private String category;
    private String categoryText;
    private String title;
    private String content;
    private String imageUrl;
    private String answer;
    private String status;
    private String statusText;
    private Long answeredBy;
    private LocalDateTime answeredAt;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InquiryDTO fromEntity(Inquiry inquiry) {
        return new InquiryDTO(
                inquiry.getId(),
                inquiry.getUserId(),
                inquiry.getProductId(),
                inquiry.getOrderId(),
                inquiry.getCategory(),
                getCategoryText(inquiry.getCategory()),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getImageUrl(),
                inquiry.getAnswer(),
                inquiry.getStatus(),
                getStatusText(inquiry.getStatus()),
                inquiry.getAnsweredBy(),
                inquiry.getAnsweredAt(),
                inquiry.getIsActive(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt()
        );
    }

    private static String getCategoryText(String category) {
        if (category == null) {
            return "기타 문의";
        }

        return switch (category) {
            case "delivery" -> "배송 문의";
            case "refund" -> "환불/교환 문의";
            case "product" -> "상품 문의";
            case "order" -> "주문/결제 문의";
            default -> "기타 문의";
        };
    }

    private static String getStatusText(String status) {
        if ("answered".equals(status)) {
            return "답변 완료";
        }

        if ("closed".equals(status)) {
            return "종료";
        }

        return "답변 대기";
    }
}