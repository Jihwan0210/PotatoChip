package com.example.potatochip.cart.service;

import com.example.potatochip.cart.repository.CartRepository;
import com.example.potatochip.cart.dto.CartDTO;
import com.example.potatochip.cart.entity.Cart;
import com.example.potatochip.cartitem.dto.CartItemDTO;
import com.example.potatochip.cartitem.entity.CartItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService{
    private final CartRepository cartRepository;
    @Override
    @Transactional
    public CartDTO getMyCart(Long buyerId){
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseGet(() ->{
                    Cart newcart = Cart.builder()
                            .buyerId(buyerId)
                            .build();
                    return cartRepository.save(newcart);
                });
        List<CartItemDTO> itemDTOList = cart.getCartItems().stream()
                .map(item -> CartItemDTO.builder()
                        .buyerId(buyerId)
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPriceSnapshot())
                        .build())
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
    public void addCartItem(CartItemDTO requestDTO) {
        Cart cart = cartRepository.findByBuyerId(requestDTO.getBuyerId())
                .orElseGet(() -> Cart.builder().buyerId(requestDTO.getBuyerId()).build());
        cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(requestDTO.getProductId()))
                .findFirst()
                .ifPresentOrElse(existingItem ->
                                existingItem.setQuantity(existingItem.getQuantity() + requestDTO.getQuantity()),
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .productId(requestDTO.getProductId())
                                    .quantity(requestDTO.getQuantity())
                                    .priceSnapshot(new java.math.BigDecimal("0"))
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
        cart.getCartItems().removeIf(item -> item.getProductId().equals(productId));

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(Long buyerId, Long productId, int quantity) {
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));
        cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        cartRepository.save(cart);
    }
}
