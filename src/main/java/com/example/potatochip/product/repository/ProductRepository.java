package com.example.potatochip.product.repository;

import com.example.potatochip.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    long countByStockQuantityLessThanEqual(Integer stock);

    long countByIsPickupAvailableTrue();

    List<Product> findByStockQuantityLessThanEqual(Integer stock);

    List<Product> findBySellerId(Long sellerId);
}