package com.example.potatochip.review.dto;

public class ReviewCreateRequest {

    private Long productId;
    private Long userId;
    private Long orderItemId;
    private Integer rating;
    private String content;
    private String imageUrl;
    private Boolean repurchaseIntent;

    public ReviewCreateRequest() {
    }

    public Long getProductId() {
        return productId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrderItemId() {
        return orderItemId;
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