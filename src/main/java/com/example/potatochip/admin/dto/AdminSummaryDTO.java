package com.example.potatochip.admin.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSummaryDTO {

    private long totalUserCount;
    private long buyerCount;
    private long sellerCount;
    private long productCount;
    private long orderCount;
    private long pendingInquiryCount;
    private long reviewCount;
    private long recentReviewCount;
}