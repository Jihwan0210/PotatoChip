package com.example.potatochip.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
@NoArgsConstructor
public class OrderRequestDTO {
    private String shippingAddress; // 배송지 주소
    private String paymentMethod;   // 결제 수단 (card, bank_transfer 등)
    private String deliveryType;    // 배송 방법 (delivery, pickup)
    private LocalDateTime pickuptime; // 픽업 시간 (픽업일 경우만)
}
