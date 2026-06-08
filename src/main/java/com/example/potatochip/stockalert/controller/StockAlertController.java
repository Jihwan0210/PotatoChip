package com.example.potatochip.stockalert.controller;

import com.example.potatochip.stockalert.dto.StockAlertResponseDTO;
import com.example.potatochip.stockalert.service.StockAlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
@RestController
public class StockAlertController {

    private final StockAlertService stockAlertService;

    public StockAlertController(StockAlertService stockAlertService) {
        this.stockAlertService = stockAlertService;
}

@GetMapping("/stock-alert")
    public List<StockAlertResponseDTO> getAlerts() {
        return stockAlertService.getLowStockProducts();
}
}
