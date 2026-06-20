package com.example.potatochip.order.service;

import com.example.potatochip.order.dto.OrderDTO;
import com.example.potatochip.order.dto.OrderRequestDTO;

import java.util.List;

public interface OrderService {
    Long OrderFromCart(Long buyerId, OrderRequestDTO requestDTO);
    List<OrderDTO> getMyOrders(Long buyerId);
}
