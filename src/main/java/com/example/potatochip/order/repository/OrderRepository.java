package com.example.potatochip.order.repository;

import com.example.potatochip.order.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerId(Long buyerId);

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems i WHERE i.productId IN :productIds ORDER BY o.createdAt DESC")
    List<Order> findByProductIdIn(@Param("productIds") List<Long> productIds);
}