package com.example.potatochip.product.service;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(Long id);

    Product createProduct(ProductDTO productDTO , MultipartFile file);

    void deleteProduct(Long id);

    void modify(ProductDTO productDTO);
}