package com.example.potatochip.admin.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailDTO {

    private AdminUserDTO user;
    private List<AdminOrderDTO> orders;
    private List<AdminReviewDTO> writtenReviews;
    private List<AdminInquiryDTO> inquiries;
    private List<AdminProductDTO> products;
    private List<AdminReviewDTO> productReviews;
}