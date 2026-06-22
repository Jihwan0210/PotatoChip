package com.example.potatochip.admin.dto;

import com.example.potatochip.inquiry.entity.Inquiry;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminInquiryDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userRole;
    private String userRoleText;
    private Long productId;
    private Long orderId;
    private String category;
    private String title;
    private String content;
    private String imageUrl;
    private String answer;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;

    public static AdminInquiryDTO fromEntity(
            Inquiry inquiry,
            String userName,
            String userRole,
            String userRoleText
    ) {
        return AdminInquiryDTO.builder()
                .id(inquiry.getId())
                .userId(inquiry.getUserId())
                .userName(userName)
                .userRole(userRole)
                .userRoleText(userRoleText)
                .productId(inquiry.getProductId())
                .orderId(inquiry.getOrderId())
                .category(inquiry.getCategory())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .imageUrl(inquiry.getImageUrl())
                .answer(inquiry.getAnswer())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .answeredAt(inquiry.getAnsweredAt())
                .build();
    }
}
