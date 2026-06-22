package com.example.potatochip.product.chat.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.chat.dto.ChatMessageDTO;
import com.example.potatochip.product.chat.dto.ChatRoomDTO;
import com.example.potatochip.product.chat.entity.ChatMessage;
import com.example.potatochip.product.chat.entity.ChatRoom;
import com.example.potatochip.product.chat.repository.ChatMessageRepository;
import com.example.potatochip.product.chat.repository.ChatRoomRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ChatRoomDTO getOrCreateRoom(Long buyerId, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        Long sellerId = product.getSeller().getId();

        if (buyerId.equals(sellerId)) {
            throw new RuntimeException("자신의 상품에 채팅할 수 없습니다.");
        }

        ChatRoom room = chatRoomRepository
                .findByBuyerIdAndSellerIdAndProductId(buyerId, sellerId, productId)
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setBuyerId(buyerId);
                    newRoom.setSellerId(sellerId);
                    newRoom.setProductId(productId);
                    return chatRoomRepository.save(newRoom);
                });

        return toRoomDTO(room, buyerId);
    }

    @Override
    public List<ChatRoomDTO> getRoomsByProduct(Long productId, Long sellerId) {
        return chatRoomRepository.findBySellerIdAndProductId(sellerId, productId)
                .stream()
                .map(room -> toRoomDTO(room, sellerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageDTO> getMessages(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(roomId)
                .stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageDTO sendMessage(Long roomId, Long senderId, String senderName, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setContent(content);
        message.setIsRead(false);
        return toMessageDTO(chatMessageRepository.save(message));
    }

    @Override
    @Transactional
    public void markAsRead(Long roomId, Long userId) {
        chatMessageRepository.markAsReadByRoomIdAndNotSender(roomId, userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return chatRoomRepository.findByBuyerIdOrSellerId(userId, userId)
                .stream()
                .mapToInt(room -> chatMessageRepository
                        .countByChatRoomIdAndIsReadFalseAndSenderIdNot(room.getId(), userId))
                .sum();
    }

    private ChatRoomDTO toRoomDTO(ChatRoom room, Long myId) {
        User buyer  = userRepository.findById(room.getBuyerId()).orElse(null);
        User seller = userRepository.findById(room.getSellerId()).orElse(null);

        ChatMessage lastMsg = chatMessageRepository
                .findTopByChatRoomIdOrderBySentAtDesc(room.getId())
                .orElse(null);

        int unread = chatMessageRepository
                .countByChatRoomIdAndIsReadFalseAndSenderIdNot(room.getId(), myId);

        return ChatRoomDTO.builder()
                .id(room.getId())
                .buyerId(room.getBuyerId())
                .buyerName(buyer  != null ? buyer.getName()  : "알 수 없음")
                .sellerId(room.getSellerId())
                .sellerName(seller != null ? seller.getName() : "알 수 없음")
                .productId(room.getProductId())
                .lastMessage(lastMsg != null ? lastMsg.getContent() : "")
                .lastMessageAt(lastMsg != null ? lastMsg.getSentAt().toString() : "")
                .unreadCount(unread)
                .build();
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        ChatMessage msg = chatMessageRepository.findById(messageId).orElseThrow();
        if (!msg.getSenderId().equals(userId)) {
            throw new RuntimeException("본인이 보낸 메시지만 삭제할 수 있습니다.");
        }
        chatMessageRepository.delete(msg);
    }

    private ChatMessageDTO toMessageDTO(ChatMessage msg) {
        User sender = userRepository.findById(msg.getSenderId()).orElse(null);
        return ChatMessageDTO.builder()
                .id(msg.getId())
                .roomId(msg.getChatRoom().getId())
                .senderId(msg.getSenderId())
                .senderName(msg.getSenderName())
                .content(msg.getContent())
                .isRead(msg.getIsRead())
                .sentAt(msg.getSentAt() != null ? msg.getSentAt().toString() : "")
                .senderEmail(sender != null ? sender.getEmail() : null)
                .build();
    }
}