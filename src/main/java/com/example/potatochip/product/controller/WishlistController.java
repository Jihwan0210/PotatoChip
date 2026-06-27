package com.example.potatochip.product.controller;

import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final JwtUtil jwtUtil;

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<?> toggle(@PathVariable Long productId,
                                    @RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token.replace("Bearer ", ""));
        boolean added = wishlistService.toggle(email, productId);
        return ResponseEntity.ok(Map.of("added", added));
    }

    @GetMapping("/wishlist")
    public ResponseEntity<List<ProductDTO>> getWishlist(
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmail(token.replace("Bearer ", ""));
        return ResponseEntity.ok(wishlistService.getWishlist(email));
    }
}