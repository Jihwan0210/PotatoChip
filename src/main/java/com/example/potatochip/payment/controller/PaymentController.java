package com.example.potatochip.payment.controller;

import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.order.dto.OrderRequestDTO;
import com.example.potatochip.order.service.OrderService;
import com.example.potatochip.payment.dto.PaymentDTO;
import com.example.potatochip.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private Long getLoginUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) token = token.substring(7);
        if (token == null) token = request.getParameter("token");
        if (token == null || !jwtUtil.validateToken(token)) throw new IllegalStateException("로그인 필요");
        return userRepository.findByEmail(jwtUtil.getEmail(token))
                .orElseThrow(() -> new RuntimeException("유저 없음")).getId();
    }

    /** 카카오페이 결제 준비 */
    @PostMapping("/kakao/ready")
    @ResponseBody
    public ResponseEntity<?> kakaoReady(@RequestBody PaymentDTO.ReadyRequest req,
                                        HttpServletRequest request) {
        Long userId;
        try { userId = getLoginUserId(request); }
        catch (Exception e) { return ResponseEntity.status(401).body("로그인 필요"); }

        // 주문 생성
        OrderRequestDTO orderReq = new OrderRequestDTO();
        orderReq.setShippingAddress(req.getShippingAddress());
        orderReq.setPaymentMethod("kakaopay");
        orderReq.setDeliveryType(req.getDeliveryType());
        orderReq.setSelectedProductIds(req.getSelectedProductIds());
        orderReq.setShippingFee(req.getShippingFee());
        Long orderId = orderService.OrderFromCart(userId, orderReq);

        PaymentDTO.ReadyResponse ready = paymentService.kakaoReady(
                orderId, req.getItemName(), req.getTotalAmount(),
                userId, req.getShippingAddress(), req.getDeliveryType(),
                req.getToken()
        );

        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "redirectUrl", ready.getNext_redirect_pc_url()
        ));
    }

    /** 카카오페이 결제 승인 콜백 */
    @GetMapping("/kakao/approve")
    public String kakaoApprove(@RequestParam String pg_token,
                               @RequestParam Long order_id,
                               HttpServletRequest request) {
        Long userId;
        try { userId = getLoginUserId(request); }
        catch (Exception e) { return "redirect:/login"; }

        paymentService.kakaoApprove(pg_token, order_id, userId);

        String token = request.getParameter("token");
        return "redirect:/orders/complete/" + order_id
                + "?token=" + (token != null ? token : "");
    }
}