package com.example.potatochip.cart.service;

import com.example.potatochip.cart.Repository.CartRepository;
import com.example.potatochip.cart.dto.CartDTO;
import com.example.potatochip.cart.entity.Cart;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return CartDTO.builder()
                .id(cart.getId())
                .buyerId(cart.getBuyerId())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
