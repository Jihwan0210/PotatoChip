package com.example.potatochip.product.chat.repository;

import com.example.potatochip.product.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long roomId);
    Optional<ChatMessage> findTopByChatRoomIdOrderBySentAtDesc(Long roomId);
    int countByChatRoomIdAndIsReadFalseAndSenderIdNot(Long roomId, Long senderId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatRoom.id = :roomId AND m.senderId != :senderId AND m.isRead = false")
    void markAsReadByRoomIdAndNotSender(@Param("roomId") Long roomId, @Param("senderId") Long senderId);
}
