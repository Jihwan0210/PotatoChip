package com.example.potatochip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "판매 랭킹 응답 DTO")
public class RankingResponseDTO {

    @Schema(description = "상품명", example = "감자칩")
    private String productName;

    @Schema(description = "총 판매량", example = "26")
    private Long totalSales;

    @Schema(description = "순위", example = "1")
    private Integer rank;

    // getter
    public String getProductName() {
        return productName;
    }

    public Long getTotalSales() {
        return totalSales;
    }

    public Integer getRank() {
        return rank;
    }

    // setter
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setTotalSales(Long totalSales) {
        this.totalSales = totalSales;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}