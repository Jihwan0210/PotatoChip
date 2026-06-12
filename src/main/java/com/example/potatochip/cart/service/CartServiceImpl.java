package com.example.potatochip.cart.service;

import com.example.potatochip.cart.repository.CartRepository;
import com.example.potatochip.cart.dto.CartDTO;
import com.example.potatochip.cart.entity.Cart;
import com.example.potatochip.cartitem.dto.CartItemDTO;
import com.example.potatochip.cartitem.entity.CartItem;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;  // 추가

    @Override
    @Transactional
    public CartDTO getMyCart(Long buyerId) {
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().buyerId(buyerId).build();
                    return cartRepository.save(newCart);
                });

        List<CartItemDTO> itemDTOList = cart.getCartItems().stream()
                .map(item -> {
                    // 상품 정보 조회 추가
                    Product product = productRepository.findById(item.getProductId())
                            .orElse(null);

                    return CartItemDTO.builder()
                            .buyerId(buyerId)
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPriceSnapshot())
                            // 추가된 필드
                            .productName(product != null ? product.getName() : "상품 없음")
                            .thumbnailUrl(product != null ? product.getThumbnailUrl() : "")
                            .origin(product != null ? product.getOrigin() : "")
                            .build();
                })
                .collect(Collectors.toList());

        return CartDTO.builder()
                .id(cart.getId())
                .buyerId(cart.getBuyerId())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(itemDTOList)
                .build();
    }
    @Override
    @Transactional
    // 🌟 여기 괄호 안도 CartItemDTO 로 맞춰주세요!
    public void addCartItem(CartItemDTO requestDTO) {
        Cart cart = cartRepository.findByBuyerId(requestDTO.getBuyerId())
                .orElseGet(() -> Cart.builder().buyerId(requestDTO.getBuyerId()).build());
        cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(requestDTO.getProductId()))
                .findFirst()
                .ifPresentOrElse(existingItem ->
                                existingItem.setQuantity(existingItem.getQuantity() + requestDTO.getQuantity()),
                        () -> {
                            Product product = productRepository.findById(requestDTO.getProductId())
                                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

                            // BigDecimal이라 그냥 바로 사용
                            BigDecimal price = product.getDiscountPrice() != null
                                    ? product.getDiscountPrice()
                                    : product.getPrice();

                            CartItem newItem = CartItem.builder()
                                    .productId(requestDTO.getProductId())
                                    .quantity(requestDTO.getQuantity())
                                    .priceSnapshot(price)
                                    .build();
                            cart.addItem(newItem);
                        }
                );
        cartRepository.save(cart);
    }


    @Override
    @Transactional
    public void removeCartItem(Long buyerId, Long productId) {
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        // 🌟 서비스(알바생)가 가방을 열어 직접 삭제 조건을 걸고 지웁니다!
        cart.getCartItems().removeIf(item -> item.getProductId().equals(productId));

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(Long buyerId, Long productId, int quantity) {
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        // 🌟 서비스(알바생)가 가방을 열어 직접 상품을 찾고 수량을 바꿉니다!
        cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        cartRepository.save(cart);
    }
}
