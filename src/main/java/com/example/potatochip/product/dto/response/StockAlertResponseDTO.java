package com.example.potatochip.product.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockAlertResponseDTO {

    private String productName;

    private Integer stockQuantity;
}
