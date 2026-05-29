package com.example.potatochip.cart.Controller;

import com.example.potatochip.cart.dto.CartDTO;
import com.example.potatochip.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    @GetMapping
    public ResponseEntity<CartDTO> getMyCart(){
        Long temporaryBuyerId = 1L;
        CartDTO cartDTO = cartService.getMyCart(temporaryBuyerId);
        return ResponseEntity.ok(cartDTO);
    }
}
