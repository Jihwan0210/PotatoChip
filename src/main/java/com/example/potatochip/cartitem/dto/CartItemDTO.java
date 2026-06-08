package com.example.potatochip.cartitem.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private Long buyerId;
    private Long productId;
    private int quantity;
    private BigDecimal price;
}
