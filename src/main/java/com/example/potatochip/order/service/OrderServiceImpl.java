package com.example.potatochip.order.service;

import com.example.potatochip.cart.entity.Cart;
import com.example.potatochip.cart.repository.CartRepository;
import com.example.potatochip.order.dto.OrderRequestDTO;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;



    @Override
    @Transactional
    public void OrderFromCart(Long buyerId, OrderRequestDTO requestDTO) {

        // 1. 유저의 장바구니를 창고에서 꺼내옵니다.
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        // 2. 혹시 빈 장바구니인데 결제 버튼을 눌렀는지 검사합니다!
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니에 담긴 상품이 없습니다.");
        }

        // 3. 총 결제 금액 계산! (모든 상품의 가격 * 수량을 더합니다)
        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 새로운 영수증(Order 마스터) 껍데기를 예쁘게 만듭니다.
        Order newOrder = Order.builder()
                .buyerId(buyerId)
                // 주문번호는 "ORD-랜덤영어숫자" 형식으로 간지나게 생성!
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .shippingAddress(requestDTO.getShippingAddress())
                .paymentMethod(requestDTO.getPaymentMethod())
                .deliveryType(requestDTO.getDeliveryType() != null ? requestDTO.getDeliveryType() : "delivery")
                .pickupDatetime(requestDTO.getPickuptime())
                .totalAmount(totalAmount)
                .totalShippingFee(BigDecimal.ZERO) // 배송비는 일단 무료!
                .status(OrderStatus.PAYMENT_COMPLETE) // 방금 만든 Enum 상태 적용!
                .build();

        // 5. 장바구니(CartItem)에 있던 물건들을 하나씩 꺼내서, 영수증 상세 내역(OrderItem)으로 변신시킵니다!
        cart.getCartItems().forEach(cartItem -> {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .sellerId(1L) // 임시: 나중엔 실제 상품 DB를 찔러서 판매자 ID를 가져와야 합니다.
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPriceSnapshot())
                    // 🌟 OrderItem 만들 때 추가했던 필수값들 채워주기!
                    .shippingFee(BigDecimal.ZERO)
                    .status(OrderStatus.PAYMENT_COMPLETE)
                    .build();

            // 영수증(Order) 껍데기에 이 상세 내역을 딱 붙여줍니다.
            newOrder.addOrderItem(orderItem);
        });

        // 6. 속이 꽉 찬 영수증을 DB 창고에 영구적으로 저장합니다! (주문 완료)
        orderRepository.save(newOrder);

        // 7. 대망의 마무리: 결제가 끝났으니 장바구니 리스트를 싹 비워버립니다! 🧹
        cart.getCartItems().clear();
        cartRepository.save(cart); // 비워진 상태를 저장!
    }
}
