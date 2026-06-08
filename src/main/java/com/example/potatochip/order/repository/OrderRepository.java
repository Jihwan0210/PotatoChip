package com.example.potatochip.order.repository;

import com.example.potatochip.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order , Long> {
    List<Order> IdOrderBy(Long buyerId);
}
