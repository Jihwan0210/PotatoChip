package com.example.potatochip.service;

import com.example.potatochip.dto.response.InventoryResponseDTO;
import com.example.potatochip.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<InventoryResponseDTO> getInventory() {

        return productRepository.findAll()
                .stream()
                .map(product -> {
                    InventoryResponseDTO dto = new InventoryResponseDTO();

                    dto.setProductName(product.getName());
                    dto.setStock(product.getStock());
                    return dto;
                })

                .collect(Collectors.toList());
    }

    }

