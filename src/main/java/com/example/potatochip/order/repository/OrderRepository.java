package com.example.potatochip.order.repository;

import com.example.potatochip.order.entity.Order;

import java.util.List;

public interface OrderRepository {
    List<Order> IdOrderBy(Long buyerId);
}
