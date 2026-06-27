package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class FarmerController {

    private final SellerService sellerService;

    @GetMapping("/farmer")
    public String farmer(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }

        User seller;
        try {
            seller = sellerService.findByEmail(authentication.getName());
            model.addAttribute("sellerName", seller.getName());
            model.addAttribute("summary",    sellerService.getSummary(seller.getId()));
            model.addAttribute("orders",     sellerService.getOrderList(seller.getId()));
            model.addAttribute("products",   sellerService.getProductList(seller.getId()));
        } catch (Exception e) {
            return "redirect:/login";
        }

        try {
            model.addAttribute("salesAnalysis", sellerService.getSalesAnalysis(seller.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("salesAnalysis", Map.of(
                "monthlyStats", List.of(), "weekdayStats", List.of(),
                "peakMonthLabel", "-", "peakDay", "-", "hasSales", false));
        }

        try {
            model.addAttribute("settlement", sellerService.getSettlementData(seller.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("settlement", Map.of(
                "thisMonthRevenue", java.math.BigDecimal.ZERO,
                "lastMonthRevenue", java.math.BigDecimal.ZERO,
                "totalRevenue",     java.math.BigDecimal.ZERO,
                "settlements",      List.of()));
        }

        return "auth/farmer";
    }

    @GetMapping("/api/seller/summary")
    @ResponseBody
    public ResponseEntity<?> getSellerSummary(Authentication authentication) {
        try {
            User seller = getSeller(authentication);
            return ResponseEntity.ok(sellerService.getSummary(seller.getId()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/seller/products")
    @ResponseBody
    public ResponseEntity<?> getSellerProducts(Authentication authentication) {
        try {
            User seller = getSeller(authentication);
            return ResponseEntity.ok(sellerService.getProductList(seller.getId()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/seller/orders")
    @ResponseBody
    public ResponseEntity<?> getSellerOrders(Authentication authentication) {
        try {
            User seller = getSeller(authentication);
            return ResponseEntity.ok(sellerService.getOrderList(seller.getId()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/api/seller/orders/{orderId}/status")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User seller = getSeller(authentication);
            sellerService.updateOrderStatus(orderId, seller.getId(), body.get("status"));
            return ResponseEntity.ok(Map.of("message", "주문 상태가 변경되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/api/seller/products/{productId}/stock")
    @ResponseBody
    public ResponseEntity<?> updateStock(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> body,
            Authentication authentication) {
        try {
            User seller = getSeller(authentication);
            sellerService.updateStock(productId, seller.getId(), body.get("stock"));
            return ResponseEntity.ok(Map.of("message", "재고가 수정되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User getSeller(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("로그인이 필요합니다.");
        }
        return sellerService.findByEmail(authentication.getName());
    }
}
