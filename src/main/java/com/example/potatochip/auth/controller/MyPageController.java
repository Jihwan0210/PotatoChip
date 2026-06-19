package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.dto.UserDTO;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.product.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.review.repository.ReviewRepository;

@RequiredArgsConstructor
@Controller
public class MyPageController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WishRepository wishRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    // 마이페이지 뷰 반환
    @GetMapping("/mypage")
    public String myPage() {
        return "auth/mypage";
    }

    // 내 정보 조회
    @GetMapping("/mypage/info")
    @ResponseBody
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(java.util.Map.of(
                "id",     user.getId(),
                "name",    user.getName(),
                "email",   user.getEmail(),
                "nickname", user.getNickname() != null ? user.getNickname() : "",
                "phone",   user.getPhone()    != null ? user.getPhone()    : "",
                "address", user.getAddress()  != null ? user.getAddress()  : "",
                "role",    user.getRole()
        ));
    }

    // 내 정보 수정
    @PutMapping("/mypage/info")
    @ResponseBody
    public ResponseEntity<?> updateMyInfo(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody UserDTO dto) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        if (dto.getName()     != null) user.setName(dto.getName());
        if (dto.getNickname() != null) user.setNickname(dto.getNickname()); //고침
        if (dto.getPhone()    != null) user.setPhone(dto.getPhone());
        if (dto.getAddress()  != null) user.setAddress(dto.getAddress());

        userRepository.save(user);

        return ResponseEntity.ok(java.util.Map.of("message", "수정 완료"));
    }

    // 비밀번호 변경
    @PostMapping("/mypage/changepw")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody java.util.Map<String, String> body) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        String currentPw  = body.get("currentPassword");
        String newPw      = body.get("newPassword");
        String confirmPw  = body.get("confirmPassword");

        if (!passwordEncoder.matches(currentPw, user.getPassword())) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "현재 비밀번호가 틀렸어요."));
        }
        if (!newPw.equals(confirmPw)) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "새 비밀번호가 일치하지 않아요."));
        }

        user.setPassword(passwordEncoder.encode(newPw));
        userRepository.save(user);

        return ResponseEntity.ok(java.util.Map.of("message", "비밀번호가 변경되었어요."));
    }

    // 찜 목록 조회
    @Transactional(readOnly = true)
    @GetMapping("/mypage/wishlist")
    @ResponseBody
    public ResponseEntity<?> getMyWishlist(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }
        String email = jwtUtil.getEmail(token);

        List<Map<String, Object>> result = wishRepository.findByUserEmail(email).stream()
                .map(w -> {
                    var p = w.getProduct();
                    Map<String, Object> item = new HashMap<>();
                    item.put("id",           p.getId());
                    item.put("name",         p.getName());
                    item.put("category",     p.getCategory());
                    item.put("price",        p.getPrice());
                    item.put("discountPrice",p.getDiscountPrice());
                    item.put("thumbnailUrl", p.getThumbnailUrl());
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 주문내역 API
    @Transactional(readOnly = true)
    @GetMapping("/mypage/orders")
    @ResponseBody
    public ResponseEntity<?> getMyOrders(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Map<String, Object>> result = orderRepository.findByBuyerIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("id", order.getId());
                    orderMap.put("orderNumber", order.getOrderNumber());
                    orderMap.put("status", order.getStatus());
                    orderMap.put("totalAmount", order.getTotalAmount());
                    orderMap.put("createdAt", order.getCreatedAt());

                    List<Map<String, Object>> items = order.getOrderItems().stream()
                            .map(orderItem -> {
                                Map<String, Object> itemMap = new HashMap<>();
                                var product = productRepository.findById(orderItem.getProductId()).orElse(null);

                                itemMap.put("orderItemId", orderItem.getId());
                                itemMap.put("productId", orderItem.getProductId());
                                itemMap.put("productName", product != null ? product.getName() : "삭제된 상품");
                                itemMap.put("thumbnailUrl", product != null ? product.getThumbnailUrl() : null);
                                itemMap.put("quantity", orderItem.getQuantity());
                                itemMap.put("price", orderItem.getPrice());
                                itemMap.put("status", orderItem.getStatus());
                                itemMap.put("reviewed", reviewRepository.existsByOrderItemIdAndUserIdAndIsActiveTrue(
                                        orderItem.getId(),
                                        user.getId()
                                ));

                                return itemMap;
                            })
                            .collect(Collectors.toList());

                    orderMap.put("items", items);
                    return orderMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


    // 내 리뷰 API
    @Transactional(readOnly = true)
    @GetMapping("/mypage/reviews")
    @ResponseBody
    public ResponseEntity<?> getMyReviews(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Map<String, Object>> result = reviewRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(review -> {
                    Map<String, Object> item = new HashMap<>();
                    var product = productRepository.findById(review.getProductId()).orElse(null);

                    item.put("reviewId", review.getReviewId());
                    item.put("productId", review.getProductId());
                    item.put("productName", product != null ? product.getName() : "삭제된 상품");
                    item.put("thumbnailUrl", product != null ? product.getThumbnailUrl() : null);
                    item.put("rating", review.getRating());
                    item.put("content", review.getContent());
                    item.put("imageUrl", review.getImageUrl());
                    item.put("repurchaseIntent", review.getRepurchaseIntent());
                    item.put("isAnonymous", review.getIsAnonymous());
                    item.put("createdAt", review.getCreatedAt());

                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 회원 탈퇴
    @PostMapping("/mypage/withdraw")
    @ResponseBody
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String authHeader,
                                      @RequestBody java.util.Map<String, String> body) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        if (!passwordEncoder.matches(body.get("password"), user.getPassword())) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "비밀번호가 틀렸어요."));
        }

        user.setIsActive(false);
        user.setDeletedAt(java.time.LocalDateTime.now());
        user.setWithdrawalReason(body.getOrDefault("reason", ""));
        userRepository.save(user);

        return ResponseEntity.ok(java.util.Map.of("message", "탈퇴가 완료되었어요."));
    }
}