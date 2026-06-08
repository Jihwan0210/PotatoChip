package com.example.potatochip.cart.Controller;

import com.example.potatochip.cart.dto.CartDTO;
import com.example.potatochip.cart.service.CartService;
import com.example.potatochip.cartitem.dto.CartItemDTO;
import com.example.potatochip.cartitem.entity.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    @GetMapping
    public String getMyCart(HttpSession session, Model model) {
        session.setAttribute("loginUserId", 1L);
        Long loggedInUserId = (Long) session.getAttribute("loginUserId");
        if (loggedInUserId == null) {
            return "redirect:/login";
        }
        CartDTO cartDTO = cartService.getMyCart(loggedInUserId);
        model.addAttribute("cart", cartDTO);
        return "cart/cart";  // 여기만 변경
    }


    @PostMapping("/items")
    public ResponseEntity<String> addItemToCart(
            @RequestBody CartItemDTO requestDTO,
            HttpSession session) {

        // 보안: 로그인한 회원인지 확인
        Long loggedInUserId = (Long) session.getAttribute("loginUserId");
        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        requestDTO.setBuyerId(loggedInUserId);
        cartService.addCartItem(requestDTO);
        return ResponseEntity.ok("상품이 성공적으로 담겼습니다!");
    }
    @PatchMapping("/items/{productId}")
    public ResponseEntity<String> updateItemQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity,
            HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute("loginUserId");
        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        cartService.updateCartItemQuantity(loggedInUserId, productId, quantity);
        return ResponseEntity.ok("수량이 변경되었습니다.");
    }
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<String> removeCartItem(
            @PathVariable Long productId,
            HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute("loginUserId");
        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        cartService.removeCartItem(loggedInUserId, productId);
        return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");
    }
}
