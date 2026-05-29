package com.example.potatochip.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewUpdateRequest {

    private Long userId;
    private Integer rating;
    private String content;
    private String imageUrl;
    private Boolean repurchaseIntent;
}