package com.example.potatochip.service;

import com.example.potatochip.dto.request.ProductRequestDTO;
import com.example.potatochip.dto.response.ProductResponseDTO;
import com.example.potatochip.entity.Product;
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
    public ProductResponseDTO getProduct(Long id) {
        return productRepository.findById(id)
                .map(ProductResponseDTO::from)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
        }

        public ProductResponseDTO createProduct(ProductRequestDTO request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product savedProduct = productRepository.save(product);

        return ProductResponseDTO.from(savedProduct);
        }
    }

