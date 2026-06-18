package com.example.potatochip.order.service;

import com.example.potatochip.order.dto.OrderRequestDTO;

public interface OrderService {
    Long OrderFromCart(Long buyerId, OrderRequestDTO requestDTO);

}
