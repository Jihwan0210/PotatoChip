package com.example.potatochip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "총 매출액", example = "156000")
public class StatsResponseDTO {

    private Long totalProducts;
    private Long totalOrders;
    private Long totalSales;
    private Long totalRevenue;

    public Long getTotalRevenue(){
        return totalRevenue;
    }

    public void setTotalRevenue(Long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts){
        this.totalProducts = totalProducts;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Long totalSales) {
        this.totalSales = totalSales;
    }



}
