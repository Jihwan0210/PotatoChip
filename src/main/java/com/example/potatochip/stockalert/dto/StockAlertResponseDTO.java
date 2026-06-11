package com.example.potatochip.stockalert.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockAlertResponseDTO {

    private String productName;

    private Integer stockQuantity;
}
