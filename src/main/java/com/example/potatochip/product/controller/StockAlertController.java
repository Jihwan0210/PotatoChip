package com.example.potatochip.product.controller;

import com.example.potatochip.product.dto.response.StockAlertResponseDTO;
import com.example.potatochip.product.service.StockAlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
@RestController
public class StockAlertController {

    private final StockAlertService stockAlertService;

    public StockAlertController(StockAlertService stockAlertService) {
        this.stockAlertService = stockAlertService;
}

@GetMapping("/stock - alert")
    public List<StockAlertResponseDTO> getAlerts() {
        return stockAlertService.getLowStockProducts();
}
}
