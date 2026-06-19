package com.example.potatochip.order.controller;

import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.service.OrderItemStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemStatusController {

    private final OrderItemStatusService orderItemStatusService;

    @PatchMapping("/{orderItemId}/status")
    public ResponseEntity<Void> updateOrderItemStatus(
            @PathVariable Long orderItemId,
            @RequestParam OrderStatus status
    ) {
        orderItemStatusService.updateStatus(orderItemId, status);
        return ResponseEntity.ok().build();
    }
}
