package com.example.potatochip.admin.dto;

import com.example.potatochip.order.entity.OrderItem;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Long sellerId;
    private String sellerName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal shippingFee;
    private String status;

    public static AdminOrderItemDTO fromEntity(
            OrderItem item,
            String productName,
            String sellerName
    ) {
        return AdminOrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(productName)
                .sellerId(item.getSellerId())
                .sellerName(sellerName)
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .shippingFee(item.getShippingFee())
                .status(item.getStatus() == null ? null : item.getStatus().name())
                .build();
    }
}