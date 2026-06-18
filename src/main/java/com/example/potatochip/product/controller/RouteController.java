package com.example.potatochip.product.controller;

import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.product.dto.route.KakaoRouteResponse;
import com.example.potatochip.product.service.route.KaKaoRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RouteController {

    private final JwtUtil jwtUtil;
    private final KaKaoRouteService kaKaoRouteService;

    @GetMapping("/api/route/kakao")
    public ResponseEntity<?> getKakaoRoute(@RequestHeader("Authorization") String authHeader,
                                           @RequestParam Long productId) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);

        try {
            String url = kaKaoRouteService.buildKakaoRouteUrl(email, productId);
            return ResponseEntity.ok(new KakaoRouteResponse(url));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}