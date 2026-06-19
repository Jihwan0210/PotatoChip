package com.example.potatochip.product.chat.service;

import com.example.potatochip.product.chat.dto.ChatMessageDTO;
import com.example.potatochip.product.chat.dto.ChatRoomDTO;
import java.util.List;

public interface ChatService {
    ChatRoomDTO getOrCreateRoom(Long buyerId, Long productId);
    List<ChatRoomDTO> getRoomsByProduct(Long productId, Long sellerId);
    List<ChatMessageDTO> getMessages(Long roomId);
    ChatMessageDTO sendMessage(Long roomId, Long senderId, String senderName, String content);
    void markAsRead(Long roomId, Long userId);
    int getUnreadCount(Long userId);
}
