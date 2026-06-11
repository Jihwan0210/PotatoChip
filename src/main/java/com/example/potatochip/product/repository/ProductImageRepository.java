package com.example.potatochip.product.repository;

import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);
}
