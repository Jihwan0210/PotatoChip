package com.example.potatochip.traking.controller;

import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.traking.dto.DeliveryTrackingDTO;
import com.example.potatochip.traking.dto.DeliverydateDTO;
import com.example.potatochip.traking.service.DeliveryTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final DeliveryTrackingService deliveryTrackingService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private Long getLoginUserId(HttpServletRequest request) {
        String token = null;
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) token = header.substring(7);
        if (token == null) token = request.getParameter("token");
        if (token == null || !jwtUtil.validateToken(token)) throw new IllegalStateException("로그인이 필요합니다.");
        String email = jwtUtil.getEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음")).getId();
    }

    /** 배송 추적 HTML 페이지 서빙: GET /delivery/tracking/{orderId} */
    @GetMapping("/tracking/{orderId}")
    public String trackingPage(@PathVariable Long orderId, HttpServletRequest request) {
        try {
            getLoginUserId(request);
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
        return "order/tracking";
    }

    /** 구매자용 배송 현황 API: GET /delivery/{orderId} */
    @GetMapping("/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getTracking(@PathVariable Long orderId, HttpServletRequest request) {
        Long currentUserId;
        try {
            currentUserId = getLoginUserId(request);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        if (!order.getBuyerId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 주문만 조회할 수 있습니다.");
        }
        DeliveryTrackingDTO dto = deliveryTrackingService.getTracking(orderId);
        return ResponseEntity.ok(dto);
    }

    /** 배송 기사 위치 갱신 API: PUT /delivery/{orderId}/location */
    @PutMapping("/{orderId}/location")
    @ResponseBody
    public ResponseEntity<?> updateLocation(@PathVariable Long orderId,
                                            @RequestBody DeliverydateDTO body,
                                            HttpServletRequest request) {
        try {
            getLoginUserId(request);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        deliveryTrackingService.updateLocation(orderId, body.getLatitude(), body.getLongitude());
        return ResponseEntity.ok().build();
    }
}