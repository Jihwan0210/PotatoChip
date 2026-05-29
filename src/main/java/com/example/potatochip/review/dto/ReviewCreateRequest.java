package com.example.potatochip.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewCreateRequest {

    private Long productId;
    private Long userId;
    private Long orderItemId;
    private Integer rating;
    private String content;
    private String imageUrl;
    private Boolean repurchaseIntent;
}