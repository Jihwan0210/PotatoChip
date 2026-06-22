package com.example.potatochip.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class OrderRequestDTO {
    private String shippingAddress; // 배송지 주소
    private String paymentMethod;   // 결제 수단 (card, bank_transfer 등)
    private String deliveryType;    // 배송 방법 (delivery, express, pickup)
    private LocalDateTime pickuptime; // 픽업 시간 (픽업일 경우만)
    private BigDecimal shippingFee; // 배송비
    private List<Long> selectedProductIds; // 상품
    private Integer pointUsed;      // 사용한 포인트
}
