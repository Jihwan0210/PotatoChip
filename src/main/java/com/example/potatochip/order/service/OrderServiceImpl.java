package com.example.potatochip.order.service;

import com.example.potatochip.cartitem.entity.CartItem;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    // MariaDB 11.6+ 기본값(innodb_snapshot_isolation=ON)에서는 REPEATABLE READ로 조회한 뒤
    // 다른 주문이 바꾼 재고 행을 UPDATE하면 오류가 난다. 재고·포인트·쿠폰은 조건부 UPDATE로
    // 최신 값 기준으로 처리하므로 READ COMMITTED로 충분하다
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Long OrderFromCart(Long buyerId, OrderRequestDTO requestDTO) {

        // 1. 장바구니 조회
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        // 2. 빈 장바구니 검사
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니에 담긴 상품이 없습니다.");
        }

        // 3. 선택된 상품 필터링
        List<Long> selectedIds = requestDTO.getSelectedProductIds();
        List<CartItem> targetItems =
                (selectedIds != null && !selectedIds.isEmpty())
                        ? cart.getCartItems().stream()
                        .filter(item -> selectedIds.contains(item.getProductId()))
                        .collect(Collectors.toList())
                        : new ArrayList<>(cart.getCartItems());

        if (targetItems.isEmpty()) {
            throw new IllegalArgumentException("선택된 상품이 없습니다.");
        }

        // 4. 주문 시점 현재가 조회 (할인 기간 반영)
        Map<Long, BigDecimal> currentPriceByProduct = new HashMap<>();
        Map<Long, Long> sellerIdByProduct = new HashMap<>();
        for (CartItem item : targetItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + item.getProductId()));
            currentPriceByProduct.put(product.getId(), product.currentPrice());
            sellerIdByProduct.put(product.getId(),
                    product.getSeller() != null ? product.getSeller().getId() : 1L);
        }

        // 총 금액 = 현재가 × 수량
        BigDecimal totalAmount = targetItems.stream()
                .map(item -> currentPriceByProduct.get(item.getProductId())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = requestDTO.getShippingFee() != null
                ? requestDTO.getShippingFee()
                : BigDecimal.ZERO;

        // 5. 쿠폰 할인 적용 (현재가 기준)
        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (requestDTO.getUserCouponId() != null) {
            Map<Long, BigDecimal> amountByProduct = targetItems.stream()
                    .collect(Collectors.toMap(
                            CartItem::getProductId,
                            item -> currentPriceByProduct.get(item.getProductId())
                                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    ));

            List<Long> orderedProductIds = new ArrayList<>(amountByProduct.keySet());

            Long couponProductId = couponService.getCouponTargetProductId(requestDTO.getUserCouponId());
            BigDecimal targetProductAmount = (couponProductId != null)
                    ? amountByProduct.get(couponProductId)
                    : null;

            couponDiscount = couponService.calculateDiscount(
                    requestDTO.getUserCouponId(), totalAmount, shippingFee, orderedProductIds, targetProductAmount);
        }

        BigDecimal discountedTotal = totalAmount.subtract(couponDiscount).max(BigDecimal.ZERO);

        // 6. 주문 생성
        Order newOrder = Order.builder()
                .buyerId(buyerId)
                .cartId(cart.getId())
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .shippingAddress(requestDTO.getShippingAddress())
                .paymentMethod(requestDTO.getPaymentMethod())
                .deliveryType(requestDTO.getDeliveryType() != null ? requestDTO.getDeliveryType() : "delivery")
                .pickupDatetime(requestDTO.getPickuptime())
                .totalAmount(discountedTotal)
                .totalShippingFee(shippingFee)
                .status(OrderStatus.PAYMENT_COMPLETE)
                .build();

        // 7. OrderItem 생성 + 원자적 재고 차감 (조건부 UPDATE로 오버셀 방지)
        targetItems.forEach(cartItem -> {
            int updated = productRepository.decreaseStock(cartItem.getProductId(), cartItem.getQuantity());
            if (updated == 0) {
                throw new IllegalStateException("재고가 부족하거나 존재하지 않는 상품입니다: " + cartItem.getProductId());
            }

            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .sellerId(sellerIdByProduct.get(cartItem.getProductId()))
                    .quantity(cartItem.getQuantity())
                    .price(currentPriceByProduct.get(cartItem.getProductId()))
                    .shippingFee(BigDecimal.ZERO)
                    .status(OrderStatus.PAYMENT_COMPLETE)
                    .build();

            newOrder.addOrderItem(orderItem);
        });

        // 8. 주문 저장
        Order savedOrder = orderRepository.save(newOrder);

        // 9. 쿠폰 사용 처리
        if (requestDTO.getUserCouponId() != null) {
            couponService.useCoupon(requestDTO.getUserCouponId());
        }

        // 10. 포인트 차감 및 구매 적립
        int pointUsed = requestDTO.getPointUsed() != null ? requestDTO.getPointUsed() : 0;
        if (pointUsed < 0) {
            throw new IllegalArgumentException("사용 포인트가 올바르지 않습니다.");
        }

        BigDecimal finalAmount = discountedTotal.subtract(BigDecimal.valueOf(pointUsed)).max(BigDecimal.ZERO);
        int earnedPoints = finalAmount.divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR).intValue();

        // 재고와 같은 조건부 UPDATE: 동시 주문에도 포인트를 덮어쓰지 않는다
        if (userRepository.usePoints(buyerId, pointUsed, earnedPoints) == 0) {
            throw new IllegalStateException("포인트가 부족합니다. (사용 요청: " + pointUsed + "P)");
        }

        // 11. 장바구니 정리
        if (selectedIds != null && !selectedIds.isEmpty()) {
            cart.getCartItems().removeIf(item -> selectedIds.contains(item.getProductId()));
            cartRepository.save(cart);
        } else {
            cartRepository.delete(cart);
        }

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
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    return OrderDTO.OrderItemDTO.builder()
                            .orderItemId(item.getId())           // 문서2에만 있던 필드
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .productName(product != null ? product.getName() : "상품 정보 없음")
                            .thumbnailUrl(product != null ? product.getThumbnailUrl() : "")
                            .status(item.getStatus() != null ? item.getStatus().name() : null)  // 문서2에만 있던 필드
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