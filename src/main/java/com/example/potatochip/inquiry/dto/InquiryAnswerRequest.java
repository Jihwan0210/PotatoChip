package com.example.potatochip.inquiry.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InquiryAnswerRequest {

    private Long adminId;
    private String answer;
}