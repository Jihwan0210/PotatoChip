package com.example.potatochip.notification.controller;

import com.example.potatochip.notification.dto.NotificationDTO;
import com.example.potatochip.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDTO> getNotifications(@RequestParam Long userId) {
        return notificationService.getNotifications(userId);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(@RequestParam Long userId) {
        return Map.of("count", notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{notificationId}/read")
    public Map<String, String> markAsRead(@PathVariable Long notificationId, @RequestParam Long userId) {
        notificationService.markAsRead(notificationId, userId);
        return Map.of("message", "읽음 처리되었습니다.");
    }

    @PatchMapping("/read-all")
    public Map<String, String> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return Map.of("message", "전체 읽음 처리되었습니다.");
    }
}
