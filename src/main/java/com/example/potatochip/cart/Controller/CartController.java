package com.example.potatochip.cart.Controller;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.cart.dto.CartDTO;
import com.example.potatochip.cart.service.CartService;
import com.example.potatochip.cartitem.dto.CartItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 페이지만 반환 (Thymeleaf NullPointerException 방지용 빈 객체)
    @GetMapping
    public String cartPage(Model model) {
        model.addAttribute("cart", new CartDTO());
        model.addAttribute("totalPrice", BigDecimal.ZERO);
        return "cart/cart";
    }

    // JS fetch로 호출하는 데이터 API
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<CartDTO> getCartData() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (email == null || email.equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        CartDTO cartDTO = cartService.getMyCart(user.getId());
        return ResponseEntity.ok(cartDTO);
    }

    // 공통 유저 ID 헬퍼
    private Long getLoginUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (email == null || email.equals("anonymousUser")) {
            throw new RuntimeException("로그인 필요");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"))
                .getId();
    }

    @PostMapping("/items")
    public ResponseEntity<String> addItemToCart(@RequestBody CartItemDTO requestDTO) {
        Long userId = getLoginUserId();
        requestDTO.setBuyerId(userId);
        cartService.addCartItem(requestDTO);
        return ResponseEntity.ok("담겼습니다!");
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<String> updateItemQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        cartService.updateCartItemQuantity(getLoginUserId(), productId, quantity);
        return ResponseEntity.ok("수량 변경됐습니다.");
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<String> removeCartItem(@PathVariable Long productId) {
        cartService.removeCartItem(getLoginUserId(), productId);
        return ResponseEntity.ok("삭제됐습니다.");
    }

    @GetMapping("/items/buy-now")
    public String buyNow(@RequestParam Long productId,
                         @RequestParam int quantity,
                         @RequestParam(required = false) String token) {

        // URL 파라미터로 토큰 받기
        if (token == null || token.isBlank()) {
            return "redirect:/login";
        }

        try {
            // 토큰에서 직접 email 꺼내기
            String email = jwtUtil.getEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("유저 없음"));

            CartItemDTO dto = CartItemDTO.builder()
                    .buyerId(user.getId())
                    .productId(productId)
                    .quantity(quantity)
                    .build();
            cartService.addCartItem(dto);
        } catch (Exception e) {
            return "redirect:/login";
        }

        return "redirect:/cart";
    }
}