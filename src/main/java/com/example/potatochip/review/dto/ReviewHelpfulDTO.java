package com.example.potatochip.review.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewHelpfulDTO {

    private Long reviewId;

    private Long helpfulCount;

    private Boolean helpfulByCurrentUser;
}