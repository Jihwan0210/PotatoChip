package com.example.potatochip.payment.controller;

import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;
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

    /**
     * 카카오페이 결제 준비 - 주문 생성 없이 결제창만 띄움
     * 주문은 approve(결제 승인) 단계에서만 생성
     */
    @PostMapping("/kakao/ready")
    @ResponseBody
    public ResponseEntity<?> kakaoReady(@RequestBody PaymentDTO.ReadyRequest req,
                                        HttpServletRequest request) {
        Long userId;
        try { userId = getLoginUserId(request); }
        catch (Exception e) { return ResponseEntity.status(401).body("로그인 필요"); }

        PaymentDTO.ReadyResponse ready = paymentService.kakaoReady(
                userId,
                req.getItemName(),
                req.getTotalAmount(),
                req.getShippingAddress(),
                req.getDeliveryType(),
                req.getSelectedProductIds(),
                req.getShippingFee(),
                req.getToken()
        );

        return ResponseEntity.ok(Map.of(
                "redirectUrl", ready.getNext_redirect_pc_url()
        ));
    }

    /**
     * 카카오페이 결제 승인 콜백 - 여기서 주문 생성
     */
    @GetMapping("/kakao/approve")
    public String kakaoApprove(@RequestParam String pg_token,
                               @RequestParam Long user_id,
                               @RequestParam(required = false) String token,
                               HttpServletRequest request) {
        try {
            Long orderId = paymentService.kakaoApprove(pg_token, user_id);
            return "redirect:/orders/complete/" + orderId
                    + "?token=" + (token != null ? token : "");
        } catch (Exception e) {
            return "redirect:/cart?error=payment_failed";
        }
    }
}