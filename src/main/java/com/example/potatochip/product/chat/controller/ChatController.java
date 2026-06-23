package com.example.potatochip.product.chat.controller;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.chat.dto.ChatMessageDTO;
import com.example.potatochip.product.chat.dto.ChatRoomDTO;
import com.example.potatochip.product.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private User getLoginUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    // 방 생성 or 조회 (구매자)
    @PostMapping("/rooms")
    @ResponseBody
    public ResponseEntity<?> getOrCreateRoom(@RequestBody Map<String, Long> body) {
        Long productId = body.get("productId");
        User buyer = getLoginUser();
        try {
            ChatRoomDTO room = chatService.getOrCreateRoom(buyer.getId(), productId);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 판매자 - 이 상품의 채팅방 목록
    @GetMapping("/rooms/seller/product/{productId}")
    @ResponseBody
    public ResponseEntity<List<ChatRoomDTO>> getSellerRooms(@PathVariable Long productId) {
        User seller = getLoginUser();
        return ResponseEntity.ok(chatService.getRoomsByProduct(productId, seller.getId()));
    }

    // 메시지 목록 조회
    @GetMapping("/rooms/{roomId}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId));
    }

    // 읽음 처리
    @PostMapping("/rooms/{roomId}/read")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long roomId) {
        chatService.markAsRead(roomId, getLoginUser().getId());
        return ResponseEntity.ok().build();
    }

    // 안 읽은 채팅 수 (nav 뱃지용)
    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getUnreadCount() {
        int count = chatService.getUnreadCount(getLoginUser().getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // 메시지 삭제 (본인 메시지만)
    @DeleteMapping("/messages/{messageId}")
    @ResponseBody
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId) {
        User me = getLoginUser();
        try {
            chatService.deleteMessage(messageId, me.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // WebSocket: 메시지 수신 → 저장 → 브로드캐스트
    @MessageMapping("/chat/{roomId}")
    public void handleMessage(@DestinationVariable Long roomId,
                              @Payload ChatMessageDTO messageDTO,
                              Principal principal) {
        if (principal == null) return;
        User sender = userRepository.findByEmail(principal.getName()).orElseThrow();
        ChatMessageDTO saved = chatService.sendMessage(
                roomId, sender.getId(), sender.getName(), messageDTO.getContent()
        );
        messagingTemplate.convertAndSend("/topic/room/" + roomId, saved);
    }
}