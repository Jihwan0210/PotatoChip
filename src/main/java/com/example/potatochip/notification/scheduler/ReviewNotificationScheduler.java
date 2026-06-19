package com.example.potatochip.notification.scheduler;

import com.example.potatochip.notification.service.NotificationService;
import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderItemRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReviewNotificationScheduler {

    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    public void sendReviewReminders() {
        LocalDateTime targetTime = LocalDateTime.now().minusDays(3);

        for (OrderItem orderItem : orderItemRepository.findByStatusAndUpdatedAtBefore(OrderStatus.DELIVERED, targetTime)) {
            Long buyerId = orderItem.getOrder().getBuyerId();

            if (reviewRepository.existsByOrderItemIdAndUserIdAndIsActiveTrue(orderItem.getId(), buyerId)) {
                continue;
            }

            String productName = productRepository.findById(orderItem.getProductId())
                    .map(Product::getName)
                    .orElse("구매 상품");

            notificationService.createReviewReminderNotification(buyerId, orderItem.getId(), productName);
        }
    }
}
