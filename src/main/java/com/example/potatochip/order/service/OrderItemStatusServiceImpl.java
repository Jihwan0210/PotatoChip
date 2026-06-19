package com.example.potatochip.order.service;

import com.example.potatochip.notification.entity.NotificationType;
import com.example.potatochip.notification.service.NotificationService;
import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderItemStatusServiceImpl implements OrderItemStatusService {

    private final OrderItemRepository orderItemRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void updateStatus(Long orderItemId, OrderStatus status) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("주문 상품을 찾을 수 없습니다."));

        orderItem.setStatus(status);

        NotificationType type = switch (status) {
            case PREPARING -> NotificationType.DELIVERY_PREPARING;
            case SHIPPING -> NotificationType.DELIVERY_SHIPPING;
            case DELIVERED -> NotificationType.DELIVERY_DELIVERED;
            default -> null;
        };

        if (type != null) {
            notificationService.createDeliveryNotification(
                    orderItem.getOrder().getBuyerId(),
                    orderItem.getOrder().getOrderNumber(),
                    "구매 상품",
                    type,
                    orderItem.getId()
            );
        }
    }

}
