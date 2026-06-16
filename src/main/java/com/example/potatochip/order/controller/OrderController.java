package com.example.potatochip.order.controller;

import com.example.potatochip.cart.dto.CartDTO;
import com.example.potatochip.cart.service.CartService;
import com.example.potatochip.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService; // 🌟 장바구니 서비스 추가 (필수!)

    @GetMapping
    public String showOrderPage(Model model) {

        // 1. 임시로 유저 ID를 1번으로 고정 (추후 로그인 세션과 연동)
        Long buyerId = 1L;

        // 2. 현재 유저의 장바구니 데이터를 꺼내옵니다.
        CartDTO cartDTO = cartService.getMyCart(buyerId);
        if (cartDTO == null || cartDTO.getItems() == null) {
            // 강제로 빈 장바구니 객체를 만들어서 에러를 막습니다!
            cartDTO = CartDTO.builder()
                    .buyerId(buyerId)
                    .items(new ArrayList<>())
                    .build();
        }
        // 3. 🌟 HTML(Thymeleaf)로 "cartDTO"라는 이름으로 데이터 전달! (이게 없어서 null 에러가 났던 겁니다)
        model.addAttribute("cartDTO", cartDTO);

        return "order/order";
    }
}