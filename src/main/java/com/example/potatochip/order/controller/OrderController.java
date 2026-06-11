package com.example.potatochip.order.controller;

import com.example.potatochip.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

}
