package com.example.potatochip.order.service;

import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.cart.entity.Cart;
import com.example.potatochip.cart.repository.CartRepository;
import com.example.potatochip.cartitem.entity.CartItem;
import com.example.potatochip.order.dto.OrderDTO;
import com.example.potatochip.order.dto.OrderRequestDTO;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Long OrderFromCart(Long buyerId, OrderRequestDTO requestDTO) {
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니에 담긴 상품이 없습니다.");
        }

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

        BigDecimal totalAmount = targetItems.stream()
                .map(item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = requestDTO.getShippingFee() != null
                ? requestDTO.getShippingFee()
                : BigDecimal.ZERO;

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

        targetItems.forEach(cartItem -> {
            Product product = productRepository.findById(cartItem.getProductId()).orElse(null);
            Long sellerId = product != null && product.getSeller() != null ? product.getSeller().getId() : 1L;

            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .sellerId(sellerId)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPriceSnapshot())
                    .shippingFee(BigDecimal.ZERO)
                    .status(OrderStatus.PAYMENT_COMPLETE)
                    .build();

            newOrder.addOrderItem(orderItem);
        });

        Order savedOrder = orderRepository.save(newOrder);

        int pointUsed = requestDTO.getPointUsed() != null ? requestDTO.getPointUsed() : 0;
        BigDecimal finalAmount = totalAmount.subtract(BigDecimal.valueOf(pointUsed)).max(BigDecimal.ZERO);
        int earnedPoints = finalAmount.divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR).intValue();

        userRepository.findById(buyerId).ifPresent(user -> {
            int current = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(Math.max(0, current - pointUsed) + earnedPoints);
            userRepository.save(user);
        });

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
                            .orderItemId(item.getId())
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .productName(product != null ? product.getName() : "상품 정보 없음")
                            .thumbnailUrl(product != null ? product.getThumbnailUrl() : "")
                            .status(item.getStatus() != null ? item.getStatus().name() : null)
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
