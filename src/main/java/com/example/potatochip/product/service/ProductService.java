package com.example.potatochip.product.service;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(Long id);

    Product createProduct(ProductDTO productDTO, MultipartFile file, String sellerEmail);

    void deleteProduct(Long id);

    void modify(ProductDTO productDTO);

    Page<ProductDTO> getProducts(String category, String keyword, String sort,  String sellerEmail, Pageable pageable);



}