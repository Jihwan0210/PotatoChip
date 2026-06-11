package com.example.potatochip.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DashboardDTO {

    private Long totalProduct;

    private Long totalBoards;

    private Long lowStockProducts;

    private Long pickupProducts;
}
