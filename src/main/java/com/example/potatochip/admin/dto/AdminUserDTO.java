package com.example.potatochip.admin.dto;

import com.example.potatochip.auth.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {

    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private String address;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long orderCount;
    private long reviewCount;
    private long inquiryCount;
    private long productCount;
    private long writtenReviewCount;
    private long productReviewCount;

    public static AdminUserDTO fromEntity(User user) {
        return AdminUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole() == null ? null : user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}