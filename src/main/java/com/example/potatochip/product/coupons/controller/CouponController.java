package com.example.potatochip.product.coupons.controller;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.coupons.dto.CouponCreateDTO;
import com.example.potatochip.product.coupons.dto.UserCouponDTO;
import com.example.potatochip.product.coupons.service.CouponService;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ── 쿠폰 관리 페이지 (관리자 / 판매자) ──────────────────────────────────
    @GetMapping("/coupon/manage")
    public String managePage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElse(null);
        if (user == null) return "redirect:/login";

        String role = user.getRole().name();
        if (!"ADMIN".equals(role) && !"SELLER".equals(role)) {
            return "redirect:/";
        }

        model.addAttribute("role", role);
        model.addAttribute("myCoupons", couponService.getMyCoupons(user.getId(), role));
        return "coupon/manage";
    }

    // ── 쿠폰 생성 (관리자 / 판매자 전용) ────────────────────────────────────
    @PostMapping("/api/coupons")
    @ResponseBody
    public ResponseEntity<?> createCoupon(@RequestBody CouponCreateDTO dto) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String role = user.getRole().name();
        if (!"ADMIN".equals(role) && !"SELLER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "권한이 없습니다."));
        }
        try {
            couponService.createCoupon(dto, user.getId(), role);
            return ResponseEntity.ok(Map.of("message", "쿠폰이 생성되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── 쿠폰 비활성화 (관리자 / 판매자 — 본인 쿠폰만) ─────────────────────
    @DeleteMapping("/api/coupons/{couponId}")
    @ResponseBody
    public ResponseEntity<?> deactivateCoupon(@PathVariable Long couponId) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String role = user.getRole().name();
        try {
            couponService.deactivateCoupon(couponId, user.getId(), role);
            return ResponseEntity.ok(Map.of("message", "쿠폰이 비활성화되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── 쿠폰 코드 등록 (구매자) ──────────────────────────────────────────────
    @PostMapping("/api/coupons/register")
    @ResponseBody
    public ResponseEntity<?> registerCoupon(@RequestBody Map<String, String> body) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "쿠폰 코드를 입력해주세요."));
        }
        try {
            couponService.registerCoupon(code, user.getId());
            return ResponseEntity.ok(Map.of("message", "쿠폰이 등록되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── 사용 가능한 쿠폰 목록 (장바구니에서 호출) ────────────────────────────
    @GetMapping("/api/coupons/my")
    @ResponseBody
    public ResponseEntity<?> getMyAvailableCoupons() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<UserCouponDTO> list = couponService.getAvailableCoupons(user.getId());
        return ResponseEntity.ok(list);
    }

    // ── 할인 금액 미리 계산 ───────────────────────────────────────────────────
    @PostMapping("/api/coupons/calculate")
    @ResponseBody
    public ResponseEntity<?> calculateDiscount(@RequestBody Map<String, Object> body) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Long userCouponId = Long.valueOf(body.get("userCouponId").toString());
            BigDecimal orderAmount = new BigDecimal(body.get("orderAmount").toString());
            BigDecimal shippingFee = body.containsKey("shippingFee")
                    ? new BigDecimal(body.get("shippingFee").toString())
                    : BigDecimal.ZERO;
            BigDecimal targetProductAmount = body.containsKey("targetProductAmount") && body.get("targetProductAmount") != null
                    ? new BigDecimal(body.get("targetProductAmount").toString())
                    : null;

            BigDecimal discount = couponService.calculateDiscount(
                    userCouponId, orderAmount, shippingFee, null, targetProductAmount);
            return ResponseEntity.ok(Map.of("discount", discount));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── 상품 쿠폰 조회 (구매자 배너용, 비로그인도 가능) ──────────────────────
    @GetMapping("/api/coupons/product/{productId}")
    @ResponseBody
    public ResponseEntity<?> getProductCoupon(@PathVariable Long productId) {
        var coupon = couponService.getProductCoupon(productId);
        if (coupon == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(coupon);
    }

    // ── 상품 쿠폰 생성 (판매자 전용) ─────────────────────────────────────────
    @PostMapping("/api/coupons/product/{productId}")
    @ResponseBody
    public ResponseEntity<?> createProductCoupon(@PathVariable Long productId,
                                                  @RequestBody CouponCreateDTO dto) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"SELLER".equals(user.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "판매자만 사용 가능합니다."));
        }
        try {
            couponService.createProductCoupon(productId, dto, user.getId());
            return ResponseEntity.ok(Map.of("message", "쿠폰이 등록되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── 쿠폰 다운로드 (구매자 — 상품 페이지에서 클릭) ───────────────────────
    @PostMapping("/api/coupons/download/{couponId}")
    @ResponseBody
    public ResponseEntity<?> downloadCoupon(@PathVariable Long couponId) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "로그인이 필요합니다."));
        try {
            couponService.downloadCoupon(couponId, user.getId());
            return ResponseEntity.ok(Map.of("message", "쿠폰이 다운로드되었습니다!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── 판매자: 본인 상품 현재 쿠폰 조회 ────────────────────────────────────
    @GetMapping("/api/coupons/product/{productId}/mine")
    @ResponseBody
    public ResponseEntity<?> getSellerProductCoupon(@PathVariable Long productId) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"SELLER".equals(user.getRole().name()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        var coupon = couponService.getSellerProductCoupon(productId, user.getId());
        if (coupon == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(coupon);
    }

    // ── 판매자 본인 상품 목록 (쿠폰 생성 시 드롭다운용) ─────────────────────
    @GetMapping("/api/coupons/seller/products")
    @ResponseBody
    public ResponseEntity<?> getSellerProducts() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"SELLER".equals(user.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var products = productRepository.findBySellerId(user.getId()).stream()
                .map(p -> Map.of("id", p.getId(), "name", p.getName()))
                .toList();
        return ResponseEntity.ok(products);
    }

    // ── 내 쿠폰 목록 (관리자/판매자 — 본인 생성 쿠폰) ───────────────────────
    @GetMapping("/api/coupons/mine")
    @ResponseBody
    public ResponseEntity<?> getMyCoupons() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String role = user.getRole().name();
        return ResponseEntity.ok(couponService.getMyCoupons(user.getId(), role));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}