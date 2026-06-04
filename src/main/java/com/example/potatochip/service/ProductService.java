package com.example.potatochip.service;

import com.example.potatochip.dto.response.ProductResponseDTO;
import com.example.potatochip.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    public List<ProductResponseDTO> getProducts() {
        return productRepository.findAll()
                .stream()
                .filter(java.util.Objects::nonNull)
                .map(ProductResponseDTO::from)
                .toList();
    }
}
