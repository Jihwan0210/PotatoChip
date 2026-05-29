package com.example.potatochip.cart.service;

import com.example.potatochip.cart.dto.CartDTO;

public interface CartService {
    CartDTO getMyCart(Long buyerId);
}
