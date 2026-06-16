package com.example.potatochip.product.service;

import com.example.potatochip.product.dto.ProductDTO;

import java.util.List;

public interface WishlistService {
    boolean toggle(String email, Long productId);
    List<ProductDTO> getWishlist(String email);
}