package com.example.potatochip.notification.controller;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        try {
            Long loginUserId = getLoginUserId(authentication);
            return ResponseEntity.ok(notificationService.getNotifications(loginUserId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        try {
            Long loginUserId = getLoginUserId(authentication);
            return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(loginUserId)));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId, Authentication authentication) {
        try {
            Long loginUserId = getLoginUserId(authentication);
            notificationService.markAsRead(notificationId, loginUserId);
            return ResponseEntity.ok(Map.of("message", "알림을 읽음 처리했습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            Long loginUserId = getLoginUserId(authentication);
            notificationService.markAllAsRead(loginUserId);
            return ResponseEntity.ok(Map.of("message", "모든 알림을 읽음 처리했습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId, Authentication authentication) {
        try {
            Long loginUserId = getLoginUserId(authentication);
            notificationService.deleteNotification(notificationId, loginUserId);
            return ResponseEntity.ok(Map.of("message", "알림이 삭제되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Long getLoginUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        String email = String.valueOf(authentication.getPrincipal());
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            email = authentication.getName();
        }

        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("로그인 사용자를 찾을 수 없습니다."));

        return user.getId();
    }
}
