package com.example.potatochip.order.entity;

public enum OrderStatus {
    PAYMENT_COMPLETE, // 결제 완료
    PREPARING,        // 상품 준비중
    SHIPPING,         // 배송 중
    DELIVERED,        // 배송 완료
    CANCELLED,        // 주문 취소
    REFUNDED          // 환불 완료
}
