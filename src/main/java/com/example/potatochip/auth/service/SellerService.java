package com.example.potatochip.auth.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService {

    private final UserRepository     userRepository;
    private final ProductRepository  productRepository;
    private final OrderRepository    orderRepository;
    private final ReviewRepository   reviewRepository;

    public Map<String, Object> getSummary(Long sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);
        var reviews = reviewRepository.findBySellerIdAndIsActiveTrueOrderByCreatedAtDesc(sellerId);

        LocalDate today        = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        List<Order> sellerOrders = getSellerOrders(sellerId);

        long todayOrders = sellerOrders.stream()
                .filter(o -> o.getCreatedAt() != null &&
                             o.getCreatedAt().toLocalDate().equals(today))
                .count();

        BigDecimal monthlyRevenue = sellerOrders.stream()
                .filter(o -> o.getCreatedAt() != null &&
                             !o.getCreatedAt().toLocalDate().isBefore(startOfMonth))
                .flatMap(o -> o.getOrderItems().stream())
                .filter(i -> sellerId.equals(i.getSellerId()) && i.getPrice() != null)
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgRating = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(com.example.potatochip.review.entity.Review::getRating)
                .average().orElse(0.0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayOrderCount", todayOrders);
        result.put("totalOrderCount", sellerOrders.size());
        result.put("monthlyRevenue",  monthlyRevenue);
        result.put("avgRating",       Math.round(avgRating * 10.0) / 10.0);
        result.put("reviewCount",     reviews.size());
        result.put("productCount",    products.size());
        return result;
    }

    public List<Map<String, Object>> getOrderList(Long sellerId) {
        List<Order> sellerOrders = getSellerOrders(sellerId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Order o : sellerOrders) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (var i : o.getOrderItems()) {
                if (!sellerId.equals(i.getSellerId())) continue;
                Map<String, Object> im = new LinkedHashMap<>();
                im.put("id",       i.getId());
                im.put("quantity", i.getQuantity());
                im.put("price",    i.getPrice() != null ? i.getPrice().toString() : "0");
                im.put("status",   i.getStatus() != null ? i.getStatus().name() : "PAYMENT_COMPLETE");
                if (i.getProductId() != null) {
                    productRepository.findById(i.getProductId())
                            .ifPresent(p -> im.put("productName", p.getName()));
                }
                if (!im.containsKey("productName")) im.put("productName", "상품");
                items.add(im);
            }
            if (items.isEmpty()) continue;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",              o.getId());
            m.put("orderNumber",     o.getOrderNumber() != null ? o.getOrderNumber() : "#" + o.getId());
            m.put("status",          o.getStatus() != null ? o.getStatus().name() : "PAYMENT_COMPLETE");
            m.put("totalAmount",     o.getTotalAmount() != null ? o.getTotalAmount().toString() : "0");
            m.put("shippingAddress", o.getShippingAddress());
            m.put("createdAt",       o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
            m.put("items",           items);
            m.put("itemCount",       items.size());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> getProductList(Long sellerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : productRepository.findBySellerId(sellerId)) {
            int stock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
            String status = stock == 0 ? "품절" : stock < 10 ? "재고 부족" : "판매 중";
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",              p.getId());
            m.put("name",            p.getName());
            m.put("category",        p.getCategory());
            m.put("price",           p.getPrice() != null ? p.getPrice().toString() : "0");
            m.put("discountPrice",   p.getDiscountPrice() != null ? p.getDiscountPrice().toString() : null);
            m.put("stockQuantity",   stock);
            m.put("expiryDate",      p.getExpiryDate() != null ? p.getExpiryDate().toString() : null);
            m.put("status",          status);
            result.add(m);
        }
        return result;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Long sellerId, String statusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        boolean owned = order.getOrderItems().stream()
                .anyMatch(i -> sellerId.equals(i.getSellerId()));
        if (!owned) throw new SecurityException("권한이 없습니다.");
        OrderStatus newStatus = OrderStatus.valueOf(statusStr);
        order.getOrderItems().stream()
                .filter(i -> sellerId.equals(i.getSellerId()))
                .forEach(i -> i.setStatus(newStatus));
        orderRepository.save(order);
    }

    private List<Order> getSellerOrders(Long sellerId) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getOrderItems() != null &&
                             o.getOrderItems().stream().anyMatch(i -> sellerId.equals(i.getSellerId())))
                .sorted(Comparator.comparing(o -> o.getCreatedAt() == null ? "" : o.getCreatedAt().toString(),
                        Comparator.reverseOrder()))
                .toList();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("사용자를 찾을 수 없습니다."));
    }
}
