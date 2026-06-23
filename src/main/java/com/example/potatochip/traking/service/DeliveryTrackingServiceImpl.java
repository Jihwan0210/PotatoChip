package com.example.potatochip.traking.service;

import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.traking.dto.DeliveryTrackingDTO;
import com.example.potatochip.traking.entity.DeliveryTracking;
import com.example.potatochip.traking.repository.DeliveryTrackingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryTrackingServiceImpl implements DeliveryTrackingService {

    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final OrderRepository orderRepository;

    @Override
    public DeliveryTrackingDTO getTracking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        DeliveryTracking tracking = deliveryTrackingRepository.findByOrderId(orderId).orElse(null);

        if (tracking == null) {
            return DeliveryTrackingDTO.builder()
                    .orderId(orderId)
                    .orderNumber(order.getOrderNumber())
                    .status(order.getStatus())
                    .shippingAddress(order.getShippingAddress())
                    .deliveryType(order.getDeliveryType())
                    .build();
        }

        return DeliveryTrackingDTO.builder()
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .latitude(tracking.getLatitude())
                .longitude(tracking.getLongitude())
                .driverName(tracking.getDriverName())
                .driverPhone(tracking.getDriverPhone())
                .shippingAddress(order.getShippingAddress())
                .deliveryType(order.getDeliveryType())
                .estimatedArrival(tracking.getEstimatedArrival())
                .updatedAt(tracking.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void updateLocation(Long orderId, Double latitude, Double longitude) {
        // 주문 존재 여부 확인
        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("존재하지 않는 주문입니다.");
        }

        DeliveryTracking tracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseGet(() -> DeliveryTracking.builder()
                        .orderId(orderId)
                        .build());

        tracking.updateLocation(latitude, longitude);
        deliveryTrackingRepository.save(tracking);
    }
}
