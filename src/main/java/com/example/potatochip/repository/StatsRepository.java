package com.example.potatochip.repository;

import com.example.potatochip.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StatsRepository extends JpaRepository<Product, Long> {

@Query(value = "SELECT COUNT(*) FROM product", nativeQuery = true)
    Long countProducts();

@Query(value = "SELECT COUNT(*) FROM order_item", nativeQuery = true)
    Long countOrders();

@Query(value = "SELECT COALESCE(SUM(quantity),0) FROM order_item",nativeQuery = true)
Long totalSales();
}
