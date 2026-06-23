package com.example.potatochip.traking.dto;

import com.example.potatochip.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구매자 화면(지도)에 내려줄 배송 추적 응답 DTO.
 * status는 DeliveryTracking이 아닌 Order.status를 그대로 실어 보낸다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackingDTO {

    private Long orderId;
    private String orderNumber;
    private OrderStatus status;       // Order.status (PREPARING, SHIPPING, DELIVERED 등)

    private Double latitude;
    private Double longitude;

    private String driverName;
    private String driverPhone;
    private String shippingAddress;
    private String deliveryType;

    private LocalDateTime estimatedArrival;
    private LocalDateTime updatedAt;  // 마지막 위치 갱신 시각
}