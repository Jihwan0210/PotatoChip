package com.example.potatochip.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private String shippingAddress;
    private String deliveryType;
    private BigDecimal totalAmount;
    private BigDecimal totalShippingFee;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private String status;
    private List<OrderItemDTO> orderItems;

    @Getter
    @Setter
    @Builder
    public static class OrderItemDTO {
        private Long orderItemId;
        private Long productId;
        private int quantity;
        private BigDecimal price;
        private String productName;
        private String thumbnailUrl;
        private String status;
    }
}
