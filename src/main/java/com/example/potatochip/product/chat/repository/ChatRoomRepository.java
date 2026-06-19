package com.example.potatochip.product.chat.repository;

import com.example.potatochip.product.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByBuyerIdAndSellerIdAndProductId(Long buyerId, Long sellerId, Long productId);
    List<ChatRoom> findBySellerIdAndProductId(Long sellerId, Long productId);
    List<ChatRoom> findByBuyerIdOrSellerId(Long buyerId, Long sellerId);
}