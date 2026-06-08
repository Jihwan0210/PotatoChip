package com.example.potatochip.stockalert.service;

import com.example.potatochip.stockalert.dto.StockAlertResponseDTO;
import com.example.potatochip.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockAlertService {

    private final ProductRepository productRepository;

    public StockAlertService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<StockAlertResponseDTO> getLowStockProducts() {

        return productRepository
                .findByStockQuantityLessThanEqual(10)
                .stream()
                .map(product -> new StockAlertResponseDTO(
                product.getName(),
                product.getStockQuantity()
                ))
                .toList();
    }
}
