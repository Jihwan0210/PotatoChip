package com.example.potatochip.review.dto;

public class ReviewUpdateRequest {

    private Long userId;
    private Integer rating;
    private String content;
    private String imageUrl;
    private Boolean repurchaseIntent;

    public ReviewUpdateRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Boolean getRepurchaseIntent() {
        return repurchaseIntent;
    }
}