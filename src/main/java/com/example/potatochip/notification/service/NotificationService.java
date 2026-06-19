package com.example.potatochip.notification.service;

import com.example.potatochip.notification.dto.NotificationDTO;
import com.example.potatochip.notification.entity.Notification;
import com.example.potatochip.notification.entity.NotificationType;
import com.example.potatochip.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;


    public List<NotificationDTO> getNotifications(Long userId) {
        validateUserId(userId);

        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }

    public long getUnreadCount(Long userId) {
        validateUserId(userId);
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        validateUserId(userId);

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        validateUserId(userId);

        notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(notification -> !Boolean.TRUE.equals(notification.getIsRead()))
                .forEach(Notification::markAsRead);
    }

    @Transactional
    public void createDeliveryNotification(Long userId, String orderNumber, String productName, NotificationType type, Long orderItemId) {
        String title = switch (type) {
            case DELIVERY_PREPARING -> "배송 준비가 시작됐어요";
            case DELIVERY_SHIPPING -> "상품이 배송 중이에요";
            case DELIVERY_DELIVERED -> "상품이 도착했어요";
            default -> "배송 상태가 변경됐어요";
        };

        String message = switch (type) {
            case DELIVERY_PREPARING -> productName + " 상품 배송 준비가 시작됐어요.";
            case DELIVERY_SHIPPING -> productName + " 상품이 배송을 시작했어요.";
            case DELIVERY_DELIVERED -> productName + " 상품이 도착했어요. 상품을 확인해 주세요.";
            default -> "주문 " + orderNumber + "의 배송 상태가 변경됐어요.";
        };

        create(userId, type, title, message, "/mypage", "ORDER_ITEM", orderItemId);
    }

    @Transactional
    public void createInquiryAnsweredNotification(Long userId, Long inquiryId, String title) {
        create(
                userId,
                NotificationType.INQUIRY_ANSWERED,
                "문의 답변이 등록됐어요",
                title + " 문의에 답변이 등록됐어요.",
                "/mypage",
                "INQUIRY",
                inquiryId
        );
    }

    @Transactional
    public void createReviewReminderNotification(Long userId, Long orderItemId, String productName) {
        if (notificationRepository.existsByUserIdAndTypeAndReferenceTypeAndReferenceId(
                userId,
                NotificationType.REVIEW_REMINDER,
                "ORDER_ITEM",
                orderItemId
        )) {
            return;
        }

        create(
                userId,
                NotificationType.REVIEW_REMINDER,
                "리뷰를 남겨주세요",
                productName + " 상품은 어떠셨나요? 리뷰를 남겨주세요.",
                "/mypage",
                "ORDER_ITEM",
                orderItemId
        );
    }

    @Transactional
    public void createPriceChangedNotification(Long userId, Long productId, String productName, BigDecimal oldPrice, BigDecimal newPrice) {
        create(
                userId,
                NotificationType.PRICE_CHANGED,
                "찜한 상품 가격이 바뀌었어요",
                productName + " 가격이 " + formatPrice(oldPrice) + "원에서 " + formatPrice(newPrice) + "원으로 변경됐어요.",
                "/market/detail?id=" + productId,
                "PRODUCT",
                productId
        );
    }

    @Transactional
    public void create(Long userId, NotificationType type, String title, String message, String targetUrl, String referenceType, Long referenceId) {
        validateUserId(userId);

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .targetUrl(targetUrl)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();

        notificationRepository.save(notification);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "-";
        }

        return String.format("%,d", price.longValue());
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
    }



}
