package com.example.potatochip.order.service;
import com.example.potatochip.cartitem.entity.CartItem;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.cart.entity.Cart;
import com.example.potatochip.cart.repository.CartRepository;
import com.example.potatochip.order.dto.OrderDTO;
import com.example.potatochip.order.dto.OrderRequestDTO;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.product.coupons.service.CouponService;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;


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

        // 선택된 상품만 필터링
        List<Long> selectedIds = requestDTO.getSelectedProductIds();
        List<CartItem> targetItems =
                (selectedIds != null && !selectedIds.isEmpty())
                        ? cart.getCartItems().stream()
                        .filter(item -> selectedIds.contains(item.getProductId()))
                        .collect(Collectors.toList())
                        : new java.util.ArrayList<>(cart.getCartItems());

        if (targetItems.isEmpty()) {
            throw new IllegalArgumentException("선택된 상품이 없습니다.");
        }

        // 선택된 상품으로만 총 금액 계산
        BigDecimal totalAmount = targetItems.stream()
                .map(item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = requestDTO.getShippingFee() != null
                ? requestDTO.getShippingFee()
                : BigDecimal.ZERO;

        // 쿠폰 할인 적용 (상품 전용 쿠폰은 해당 상품 금액에만 적용)
        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (requestDTO.getUserCouponId() != null) {
            java.util.Map<Long, BigDecimal> amountByProduct = targetItems.stream()
                    .collect(Collectors.toMap(
                            CartItem::getProductId,
                            item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()))));

            List<Long> orderedProductIds = new java.util.ArrayList<>(amountByProduct.keySet());

            Long couponProductId = couponService.getCouponTargetProductId(requestDTO.getUserCouponId());
            BigDecimal targetProductAmount = (couponProductId != null)
                    ? amountByProduct.get(couponProductId)
                    : null;

            couponDiscount = couponService.calculateDiscount(
                    requestDTO.getUserCouponId(), totalAmount, shippingFee, orderedProductIds, targetProductAmount);
        }
        BigDecimal discountedTotal = totalAmount.subtract(couponDiscount).max(BigDecimal.ZERO);

        Order newOrder = Order.builder()
                .buyerId(buyerId)
                // 🌟 orders.cart_id가 NOT NULL이라 반드시 채워줘야 합니다!
                //    (이게 빠져서 "Column 'cart_id' cannot be null" 에러가 났던 부분)
                .cartId(cart.getId())
                // 주문번호는 "ORD-랜덤영어숫자" 형식으로 간지나게 생성!
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .shippingAddress(requestDTO.getShippingAddress())
                .paymentMethod(requestDTO.getPaymentMethod())
                .deliveryType(requestDTO.getDeliveryType() != null ? requestDTO.getDeliveryType() : "delivery")
                .pickupDatetime(requestDTO.getPickuptime())
                .totalAmount(discountedTotal) //쿠폰할인 토탈
                .totalShippingFee(shippingFee)
                .status(OrderStatus.PAYMENT_COMPLETE)
                .build();

        // 선택된 상품만 OrderItem으로 변환
        targetItems.forEach(cartItem -> {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .sellerId(1L)
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
        Order savedOrder = orderRepository.save(newOrder);

        // 7. 쿠폰 사용 처리
        if (requestDTO.getUserCouponId() != null) {
            couponService.useCoupon(requestDTO.getUserCouponId());
        }

        // 8. 포인트 차감 및 구매 적립 처리
        int pointUsed = requestDTO.getPointUsed() != null ? requestDTO.getPointUsed() : 0;
        BigDecimal finalAmount = discountedTotal.subtract(BigDecimal.valueOf(pointUsed)).max(BigDecimal.ZERO);
        int earnedPoints = finalAmount.divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR).intValue();

        userRepository.findById(buyerId).ifPresent(user -> {
            int current = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(Math.max(0, current - pointUsed) + earnedPoints);
            userRepository.save(user);
        });

        // 9. 장바구니 정리
        if (selectedIds != null && !selectedIds.isEmpty()) {
            // 선택한 상품만 제거, 나머지는 장바구니에 남김
            cart.getCartItems().removeIf(item -> selectedIds.contains(item.getProductId()));
            cartRepository.save(cart);
        } else {
            // 전체 주문 → 장바구니 통째로 삭제
            cartRepository.delete(cart);
        }

        // 방금 저장된 주문의 ID를 반환합니다!
        return savedOrder.getId();
    }

    @Override
    public List<OrderDTO> getMyOrders(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream()
                .map(this::toOrderDTO)
                .collect(Collectors.toList());
    }
    public OrderDTO toOrderDTO(Order order) {
        List<OrderDTO.OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElse(null);
                    return OrderDTO.OrderItemDTO.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .productName(product != null ? product.getName() : "상품 정보 없음")
                            .thumbnailUrl(product != null ? product.getThumbnailUrl() : "")
                            .build();
                })
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