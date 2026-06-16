package com.example.potatochip.admin.dto;

import com.example.potatochip.order.entity.Order;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderDTO {

    private Long id;
    private String orderNumber;
    private Long buyerId;
    private String buyerName;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private BigDecimal totalShippingFee;
    private String paymentMethod;
    private String deliveryType;
    private String status;
    private LocalDateTime createdAt;
    private int itemCount;
    private List<AdminOrderItemDTO> items;

    public static AdminOrderDTO fromEntity(
            Order order,
            String buyerName,
            List<AdminOrderItemDTO> items
    ) {
        return AdminOrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerId(order.getBuyerId())
                .buyerName(buyerName)
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .totalShippingFee(order.getTotalShippingFee())
                .paymentMethod(order.getPaymentMethod())
                .deliveryType(order.getDeliveryType())
                .status(order.getStatus() == null ? null : order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .itemCount(items == null ? 0 : items.size())
                .items(items)
                .build();
    }
}