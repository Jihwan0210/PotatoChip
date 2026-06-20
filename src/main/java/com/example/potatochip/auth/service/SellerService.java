package com.example.potatochip.auth.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, Object> getSummary(Long sellerId) {
        List<Product> products   = productRepository.findBySellerId(sellerId);
        List<Long>    myProductIds = products.stream().map(Product::getId).toList();
        var reviews              = reviewRepository.findBySellerIdAndIsActiveTrueOrderByCreatedAtDesc(sellerId);

        LocalDate today        = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        List<Order> sellerOrders = getSellerOrders(sellerId);

        long todayOrders = sellerOrders.stream()
                .filter(o -> o.getCreatedAt() != null &&
                             o.getCreatedAt().toLocalDate().equals(today))
                .count();

        // sellerId 하드코딩 문제 우회: 상품 ID 목록으로 필터
        BigDecimal monthlyRevenue = sellerOrders.stream()
                .filter(o -> o.getCreatedAt() != null &&
                             !o.getCreatedAt().toLocalDate().isBefore(startOfMonth))
                .flatMap(o -> o.getOrderItems().stream())
                .filter(i -> myProductIds.contains(i.getProductId()) && i.getPrice() != null)
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
        List<Long> myProductIds = productRepository.findBySellerId(sellerId)
                .stream().map(Product::getId).toList();
        List<Order> sellerOrders = getSellerOrders(sellerId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Order o : sellerOrders) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (var i : o.getOrderItems()) {
                if (!myProductIds.contains(i.getProductId())) continue;
                Map<String, Object> im = new LinkedHashMap<>();
                im.put("id",          i.getId());
                im.put("quantity",    i.getQuantity());
                im.put("price",       i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO);
                im.put("status",      i.getStatus() != null ? i.getStatus().name() : "PAYMENT_COMPLETE");
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
            m.put("totalAmount",     o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
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
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",            p.getId());
            m.put("name",          p.getName());
            m.put("category",      p.getCategory());
            m.put("price",         p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO);
            m.put("discountPrice", p.getDiscountPrice());
            m.put("stockQuantity", stock);
            m.put("expiryDate",    p.getExpiryDate() != null ? p.getExpiryDate().toString() : null);
            result.add(m);
        }
        return result;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Long sellerId, String statusStr) {
        List<Long> myProductIds = productRepository.findBySellerId(sellerId)
                .stream().map(Product::getId).toList();
        if (myProductIds.isEmpty()) throw new SecurityException("권한이 없습니다.");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        boolean owned = order.getOrderItems().stream()
                .anyMatch(i -> myProductIds.contains(i.getProductId()));
        if (!owned) throw new SecurityException("권한이 없습니다.");

        OrderStatus newStatus = OrderStatus.valueOf(statusStr);

        // order_items 테이블 직접 UPDATE (dirty checking 우회)
        entityManager.createQuery(
                "UPDATE OrderItem i SET i.status = :status, i.updatedAt = :now" +
                " WHERE i.order.id = :orderId AND i.productId IN :productIds")
                .setParameter("status", newStatus)
                .setParameter("now", java.time.LocalDateTime.now())
                .setParameter("orderId", orderId)
                .setParameter("productIds", myProductIds)
                .executeUpdate();

        // orders 테이블 직접 UPDATE
        entityManager.createQuery(
                "UPDATE Order o SET o.status = :status, o.updatedAt = :now WHERE o.id = :orderId")
                .setParameter("status", newStatus)
                .setParameter("now", java.time.LocalDateTime.now())
                .setParameter("orderId", orderId)
                .executeUpdate();
    }

    public Map<String, Object> getSalesAnalysis(Long sellerId) {
        List<Long> myProductIds = productRepository.findBySellerId(sellerId)
                .stream().map(Product::getId).toList();
        List<Order> sellerOrders = getSellerOrders(sellerId);
        LocalDate now = LocalDate.now();

        // ── 최근 6개월 월별 매출 ──────────────────────────────
        LinkedHashMap<String, BigDecimal> monthRevMap = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            monthRevMap.put(String.format("%d.%02d", m.getYear(), m.getMonthValue()), BigDecimal.ZERO);
        }
        for (Order o : sellerOrders) {
            if (o.getCreatedAt() == null) continue;
            LocalDate d = o.getCreatedAt().toLocalDate();
            String key = String.format("%d.%02d", d.getYear(), d.getMonthValue());
            if (!monthRevMap.containsKey(key)) continue;
            BigDecimal rev = o.getOrderItems().stream()
                    .filter(i -> myProductIds.contains(i.getProductId()) && i.getPrice() != null)
                    .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthRevMap.merge(key, rev, BigDecimal::add);
        }
        BigDecimal maxRev = monthRevMap.values().stream()
                .max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        if (maxRev.compareTo(BigDecimal.ZERO) == 0) maxRev = BigDecimal.ONE;

        String curMonthKey = String.format("%d.%02d", now.getYear(), now.getMonthValue());
        String peakMonthLabel = curMonthKey;
        List<Map<String, Object>> monthlyStats = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : monthRevMap.entrySet()) {
            if (e.getValue().compareTo(maxRev) == 0) peakMonthLabel = e.getKey();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label",     e.getKey().substring(5) + "월");
            m.put("revenue",   e.getValue());
            m.put("heightPct", e.getValue().multiply(BigDecimal.valueOf(100))
                    .divide(maxRev, 0, RoundingMode.HALF_UP).intValue());
            m.put("isCurrent", e.getKey().equals(curMonthKey));
            monthlyStats.add(m);
        }

        // ── 요일별 주문 패턴 ──────────────────────────────────
        long[] wdCounts = new long[7];
        for (Order o : sellerOrders) {
            if (o.getCreatedAt() == null) continue;
            wdCounts[o.getCreatedAt().getDayOfWeek().getValue() - 1]++;
        }
        long maxWd = 1; int peakIdx = 0;
        for (int i = 0; i < 7; i++) {
            if (wdCounts[i] > maxWd) maxWd = wdCounts[i];
            if (wdCounts[i] > wdCounts[peakIdx]) peakIdx = i;
        }
        String[] dayNames = {"월","화","수","목","금","토","일"};
        int todayIdx = now.getDayOfWeek().getValue() - 1;
        List<Map<String, Object>> weekdayStats = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("day",       dayNames[i]);
            m.put("count",     wdCounts[i]);
            m.put("heightPct", (int)(wdCounts[i] * 100 / maxWd));
            m.put("isToday",   i == todayIdx);
            weekdayStats.add(m);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("monthlyStats",   monthlyStats);
        result.put("weekdayStats",   weekdayStats);
        result.put("peakMonthLabel", peakMonthLabel.substring(5) + "월");
        result.put("peakDay",        dayNames[peakIdx]);
        result.put("hasSales",       sellerOrders.stream().anyMatch(o -> o.getCreatedAt() != null));
        return result;
    }

    public Map<String, Object> getSettlementData(Long sellerId) {
        List<Long> myProductIds = productRepository.findBySellerId(sellerId)
                .stream().map(Product::getId).toList();
        List<Order> sellerOrders = getSellerOrders(sellerId);
        LocalDate now = LocalDate.now();

        // 최근 12개월 초기화
        LinkedHashMap<String, BigDecimal> monthRevMap = new LinkedHashMap<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            monthRevMap.put(String.format("%d.%02d", m.getYear(), m.getMonthValue()), BigDecimal.ZERO);
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order o : sellerOrders) {
            if (o.getCreatedAt() == null) continue;
            BigDecimal rev = o.getOrderItems().stream()
                    .filter(i -> myProductIds.contains(i.getProductId()) && i.getPrice() != null)
                    .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalRevenue = totalRevenue.add(rev);
            String key = String.format("%d.%02d",
                    o.getCreatedAt().toLocalDate().getYear(),
                    o.getCreatedAt().toLocalDate().getMonthValue());
            if (monthRevMap.containsKey(key)) monthRevMap.merge(key, rev, BigDecimal::add);
        }

        String curKey  = String.format("%d.%02d", now.getYear(), now.getMonthValue());
        String prevKey = String.format("%d.%02d",
                now.minusMonths(1).getYear(), now.minusMonths(1).getMonthValue());

        List<Map<String, Object>> settlements = new ArrayList<>();
        List<String> keys = new ArrayList<>(monthRevMap.keySet());
        Collections.reverse(keys);
        for (String k : keys) {
            BigDecimal amount = monthRevMap.get(k);
            if (amount.compareTo(BigDecimal.ZERO) == 0) continue;
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("month",     k);
            s.put("amount",    amount);
            s.put("isCurrent", k.equals(curKey));
            s.put("status",    k.equals(curKey) ? "예정" : "완료");
            settlements.add(s);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("thisMonthRevenue", monthRevMap.getOrDefault(curKey,  BigDecimal.ZERO));
        result.put("lastMonthRevenue", monthRevMap.getOrDefault(prevKey, BigDecimal.ZERO));
        result.put("totalRevenue",     totalRevenue);
        result.put("settlements",      settlements);
        return result;
    }

    @Transactional
    public void updateStock(Long productId, Long sellerId, int quantity) {
        int updated = entityManager.createQuery(
                "UPDATE Product p SET p.stockQuantity = :quantity, p.updatedAt = :now" +
                " WHERE p.id = :productId AND p.seller.id = :sellerId")
                .setParameter("quantity", quantity)
                .setParameter("now", java.time.LocalDateTime.now())
                .setParameter("productId", productId)
                .setParameter("sellerId", sellerId)
                .executeUpdate();
        if (updated == 0) throw new SecurityException("권한이 없거나 상품을 찾을 수 없습니다.");
    }

    private List<Order> getSellerOrders(Long sellerId) {
        List<Long> myProductIds = productRepository.findBySellerId(sellerId)
                .stream().map(Product::getId).toList();

        return orderRepository.findAll().stream()
                .filter(o -> o.getOrderItems() != null &&
                             o.getOrderItems().stream().anyMatch(i -> myProductIds.contains(i.getProductId())))
                .sorted(Comparator.comparing(o -> o.getCreatedAt() == null ? "" : o.getCreatedAt().toString(),
                        Comparator.reverseOrder()))
                .toList();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("사용자를 찾을 수 없습니다."));
    }
}
