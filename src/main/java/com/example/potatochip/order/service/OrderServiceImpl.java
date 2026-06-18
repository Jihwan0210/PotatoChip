package com.example.potatochip.order.service;

import com.example.potatochip.cart.entity.Cart;
import com.example.potatochip.cart.repository.CartRepository;
import com.example.potatochip.order.dto.OrderDTO;
import com.example.potatochip.order.dto.OrderRequestDTO;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;


    @Override
    @Transactional
    public Long OrderFromCart(Long buyerId, OrderRequestDTO requestDTO) {

        // 1. 유저의 장바구니를 창고에서 가져온다
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        // 2. 혹시 빈 장바구니인데 결제 버튼을 눌렀는지 검사
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니에 담긴 상품이 없습니다.");
        }

        // 3. 총 결제 금액 계산 (모든 상품의 가격 * 수량)
        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 배송비: order.html에서 넘어온 값 사용, 없으면 0
        BigDecimal shippingFee = requestDTO.getShippingFee() != null
                ? requestDTO.getShippingFee()
                : BigDecimal.ZERO;

        // 5. 새로운 Order 생성
        Order newOrder = Order.builder()
                .buyerId(buyerId)
                .cartId(cart.getId())
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .shippingAddress(requestDTO.getShippingAddress())
                .paymentMethod(requestDTO.getPaymentMethod())
                .deliveryType(requestDTO.getDeliveryType() != null ? requestDTO.getDeliveryType() : "delivery")
                .pickupDatetime(requestDTO.getPickuptime())
                .totalAmount(totalAmount)
                .totalShippingFee(shippingFee)
                .status(OrderStatus.PAYMENT_COMPLETE)
                .build();

        // 6. CartItem → OrderItem 변환
        cart.getCartItems().forEach(cartItem -> {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .sellerId(1L)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPriceSnapshot())
                    .shippingFee(BigDecimal.ZERO)
                    .status(OrderStatus.PAYMENT_COMPLETE)
                    .build();
            newOrder.addOrderItem(orderItem);
        });

        // 7. DB에 저장
        Order savedOrder = orderRepository.save(newOrder);

        // 8. 장바구니 비우기
        cart.getCartItems().clear();
        cartRepository.save(cart);

        // 9. 저장된 주문 ID 반환
        return savedOrder.getId();
    }

    @Override
    public List<OrderDTO> getMyOrders(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream()
                .map(this::toOrderDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO toOrderDTO(Order order) {
        List<OrderDTO.OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> OrderDTO.OrderItemDTO.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .shippingAddress(order.getShippingAddress())
                .deliveryType(order.getDeliveryType())
                .totalAmount(order.getTotalAmount())
                .totalShippingFee(order.getTotalShippingFee())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .orderItems(itemDTOs)
                .build();
    }
}