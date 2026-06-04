package com.example.potatochip.dto.response;

public class StatsResponseDTO {

    private Long totalProducts;
    private Long totalOrders;
    private Long totalSales;

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
