package com.example.potatochip.order.service;

import com.example.potatochip.order.entity.OrderStatus;

public interface OrderItemStatusService {
    void updateStatus(Long orderItemId, OrderStatus status);
}
