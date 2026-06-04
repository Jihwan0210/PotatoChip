package com.example.potatochip.product.service;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;

import java.util.List;

public interface ProductService {

    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(Long id);

    Long createProduct(ProductDTO productDTO);

    void deleteProduct(Long id);

    void modify(ProductDTO productDTO);
}