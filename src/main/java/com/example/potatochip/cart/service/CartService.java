package com.example.potatochip.cart.service;

import com.example.potatochip.cart.dto.CartDTO;
import com.example.potatochip.cartitem.dto.CartItemDTO;

public interface CartService {
    CartDTO getMyCart(Long buyerId);
    void addCartItem(CartItemDTO requestDTO);
    void removeCartItem(Long buyerId, Long productId);
    void updateCartItemQuantity(Long buyerId, Long productId, int quantity);
}
