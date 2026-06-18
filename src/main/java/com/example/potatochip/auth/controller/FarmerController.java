package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class FarmerController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping("/farmer")
    public String farmer() {
        return "auth/farmer";
    }

    /** 판매자 KPI 요약 */
    @GetMapping("/api/seller/summary")
    @ResponseBody
    public ResponseEntity<?> getSellerSummary(Authentication authentication) {
        try {
            User seller = getSellerUser(authentication);
            Long sellerId = seller.getId();

            var products = productRepository.findBySellerId(sellerId);
            var reviews  = reviewRepository.findBySellerIdAndIsActiveTrueOrderByCreatedAtDesc(sellerId);

            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);

            List<Order> sellerOrders = orderRepository.findAll().stream()
                    .filter(o -> o.getOrderItems() != null &&
                                 o.getOrderItems().stream().anyMatch(i -> sellerId.equals(i.getSellerId())))
                    .toList();

            long todayOrders = sellerOrders.stream()
                    .filter(o -> o.getCreatedAt() != null &&
                                 o.getCreatedAt().toLocalDate().equals(today))
                    .count();

            BigDecimal monthlyRevenue = sellerOrders.stream()
                    .filter(o -> o.getCreatedAt() != null &&
                                 !o.getCreatedAt().toLocalDate().isBefore(startOfMonth))
                    .flatMap(o -> o.getOrderItems().stream())
                    .filter(i -> sellerId.equals(i.getSellerId()))
                    .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double avgRating = reviews.stream()
                    .filter(r -> r.getRating() != null)
                    .mapToInt(com.example.potatochip.review.entity.Review::getRating)
                    .average()
                    .orElse(0.0);

            return ResponseEntity.ok(Map.of(
                    "todayOrderCount", todayOrders,
                    "totalOrderCount", sellerOrders.size(),
                    "monthlyRevenue",  monthlyRevenue,
                    "avgRating",       Math.round(avgRating * 10.0) / 10.0,
                    "reviewCount",     reviews.size(),
                    "productCount",    products.size()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /** 판매자 상품 목록 (재고 관리) */
    @GetMapping("/api/seller/products")
    @ResponseBody
    public ResponseEntity<?> getSellerProducts(Authentication authentication) {
        try {
            User seller = getSellerUser(authentication);

            List<Map<String, Object>> result = productRepository.findBySellerId(seller.getId()).stream()
                    .map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id",              p.getId());
                        m.put("name",            p.getName());
                        m.put("category",        p.getCategory());
                        m.put("price",           p.getPrice());
                        m.put("discountPrice",   p.getDiscountPrice());
                        m.put("stockQuantity",   p.getStockQuantity());
                        m.put("pickupAvailable", p.getIsPickupAvailable());
                        m.put("thumbnailUrl",    p.getThumbnailUrl());
                        m.put("expiryDate",      p.getExpiryDate());
                        m.put("createdAt",       p.getCreatedAt());

                        int stock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
                        String status = stock == 0 ? "품절" : stock < 10 ? "재고 부족" : "판매 중";
                        m.put("status", status);
                        return m;
                    })
                    .toList();

            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /** 판매자 주문 목록 */
    @GetMapping("/api/seller/orders")
    @ResponseBody
    public ResponseEntity<?> getSellerOrders(Authentication authentication) {
        try {
            User seller = getSellerUser(authentication);
            Long sellerId = seller.getId();

            List<Map<String, Object>> result = orderRepository.findAll().stream()
                    .filter(o -> o.getOrderItems() != null &&
                                 o.getOrderItems().stream().anyMatch(i -> sellerId.equals(i.getSellerId())))
                    .sorted((a, b) -> {
                        if (a.getCreatedAt() == null) return 1;
                        if (b.getCreatedAt() == null) return -1;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    })
                    .map(o -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id",              o.getId());
                        m.put("orderNumber",     o.getOrderNumber());
                        m.put("status",          o.getStatus() != null ? o.getStatus().name() : "PAYMENT_COMPLETE");
                        m.put("totalAmount",     o.getTotalAmount());
                        m.put("deliveryType",    o.getDeliveryType());
                        m.put("shippingAddress", o.getShippingAddress());
                        m.put("createdAt",       o.getCreatedAt());

                        List<Map<String, Object>> items = o.getOrderItems().stream()
                                .filter(i -> sellerId.equals(i.getSellerId()))
                                .map(i -> {
                                    Map<String, Object> im = new LinkedHashMap<>();
                                    im.put("id",       i.getId());
                                    im.put("quantity", i.getQuantity());
                                    im.put("price",    i.getPrice());
                                    im.put("status",   i.getStatus() != null ? i.getStatus().name() : "PAYMENT_COMPLETE");
                                    productRepository.findById(i.getProductId())
                                            .ifPresent(p -> im.put("productName", p.getName()));
                                    if (!im.containsKey("productName"))
                                        im.put("productName", "상품 " + i.getProductId());
                                    return im;
                                })
                                .toList();

                        m.put("items",     items);
                        m.put("itemCount", items.size());
                        return m;
                    })
                    .toList();

            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /** 주문 상태 변경 */
    @PatchMapping("/api/seller/orders/{orderId}/status")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User seller = getSellerUser(authentication);
            Long sellerId = seller.getId();

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

            boolean owned = order.getOrderItems().stream()
                    .anyMatch(i -> sellerId.equals(i.getSellerId()));
            if (!owned) return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));

            OrderStatus newStatus = OrderStatus.valueOf(body.get("status"));
            order.getOrderItems().stream()
                    .filter(i -> sellerId.equals(i.getSellerId()))
                    .forEach(i -> i.setStatus(newStatus));
            orderRepository.save(order);

            return ResponseEntity.ok(Map.of("message", "주문 상태가 변경되었습니다.", "status", newStatus.name()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User getSellerUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("로그인이 필요합니다.");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecurityException("사용자를 찾을 수 없습니다."));
    }
}
