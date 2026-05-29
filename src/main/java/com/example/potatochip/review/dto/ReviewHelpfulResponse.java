package com.example.potatochip.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewHelpfulResponse {

    private Long reviewId;
    private Long helpfulCount;
    private Boolean helpfulByCurrentUser;
}