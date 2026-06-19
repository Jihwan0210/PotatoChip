package com.example.potatochip.order.controller;

import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.cart.dto.CartDTO;
import com.example.potatochip.cart.service.CartService;
import com.example.potatochip.order.dto.OrderDTO;
import com.example.potatochip.order.dto.OrderRequestDTO;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.order.service.OrderService;
import com.example.potatochip.order.service.OrderServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderServiceImpl orderServiceImpl;
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private Long getLoginUserId(HttpServletRequest request) {
        String token = null;

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (token == null) {
            token = request.getParameter("token");
        }

        if (token == null || !jwtUtil.validateToken(token)) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"))
                .getId();
    }

    @GetMapping
    public String showOrderPage(@RequestParam(required = false) String token,
                                Model model,
                                HttpServletRequest request) {
        Long buyerId;
        try {
            buyerId = getLoginUserId(request);
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }

        CartDTO cartDTO = cartService.getMyCart(buyerId);
        if (cartDTO == null || cartDTO.getItems() == null) {
            cartDTO = CartDTO.builder()
                    .buyerId(buyerId)
                    .items(new ArrayList<>())
                    .build();
        }
        model.addAttribute("cartDTO", cartDTO);
        return "order/order";
    }

    @PostMapping("/checkout")
    public String checkoutOrder(OrderRequestDTO requestDTO,
                                HttpServletRequest request) {
        Long buyerId;
        String token = null;
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }
            if (token == null) {
                token = request.getParameter("token");
            }
            buyerId = getLoginUserId(request);
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }

        Long newOrderId = orderService.OrderFromCart(buyerId, requestDTO);
        return "redirect:/orders/complete/" + newOrderId + "?token=" + token;
    }

    @GetMapping("/complete/{orderId}")
    public String showCompletePage(@PathVariable Long orderId,
                                   Model model,
                                   HttpServletRequest request) {
        Long currentUserId;
        try {
            currentUserId = getLoginUserId(request);
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (!order.getBuyerId().equals(currentUserId)) {
            throw new IllegalArgumentException("본인의 주문만 조회할 수 있습니다.");
        }

        OrderDTO orderDTO = orderServiceImpl.toOrderDTO(order);

        model.addAttribute("order", orderDTO);
        return "order/checkout";
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders(HttpServletRequest request) {
        Long buyerId;
        try {
            buyerId = getLoginUserId(request);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        List<OrderDTO> myOrders = orderService.getMyOrders(buyerId);
        return ResponseEntity.ok(myOrders);
    }
}