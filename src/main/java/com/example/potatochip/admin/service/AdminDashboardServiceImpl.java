package com.example.potatochip.admin.service;
import com.example.potatochip.admin.dto.*;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.inquiry.entity.Inquiry;
import com.example.potatochip.inquiry.repository.InquiryRepository;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.review.entity.Review;
import com.example.potatochip.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final InquiryRepository inquiryRepository;

    @Override
    public AdminSummaryDTO getSummary() {
        List<User> users = userRepository.findAll();
        List<Review> reviews = reviewRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        long buyerCount = users.stream()
                .filter(user -> hasRole(user, "BUYER"))
                .count();

        long sellerCount = users.stream()
                .filter(user -> hasRole(user, "SELLER"))
                .count();

        return AdminSummaryDTO.builder()
                // 관리자 ADMIN은 전체 회원 수에서 제외
                .totalUserCount(buyerCount + sellerCount)
                .buyerCount(buyerCount)
                .sellerCount(sellerCount)
                .productCount(productRepository.count())
                .orderCount(orderRepository.count())
                .pendingInquiryCount(inquiryRepository.countByStatusAndIsActiveTrue("pending"))
                .reviewCount(reviewRepository.countByIsActiveTrue())
                .recentReviewCount(reviews.stream()
                        .filter(review -> review.getCreatedAt() != null)
                        .filter(review -> review.getCreatedAt().isAfter(sevenDaysAgo))
                        .count())
                .build();
    }


    @Override
    public List<AdminUserDTO> getBuyers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> hasRole(user, "BUYER"))
                .sorted((a, b) -> dateOrMin(b.getCreatedAt()).compareTo(dateOrMin(a.getCreatedAt())))
                .map(user -> {
                    AdminUserDTO dto = AdminUserDTO.fromEntity(user);
                    dto.setOrderCount(orderRepository.findByBuyerId(user.getId()).size());
                    dto.setReviewCount(reviewRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(user.getId()).size());
                    dto.setInquiryCount(inquiryRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(user.getId()).size());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<AdminUserDTO> getSellers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> hasRole(user, "SELLER"))
                .sorted((a, b) -> dateOrMin(b.getCreatedAt()).compareTo(dateOrMin(a.getCreatedAt())))
                .map(user -> {
                    AdminUserDTO dto = AdminUserDTO.fromEntity(user);

                    long productCount = productRepository.findAll()
                            .stream()
                            .filter(product -> product.getSeller() != null)
                            .filter(product -> Objects.equals(product.getSeller().getId(), user.getId()))
                            .count();

                    dto.setProductCount(productCount);
                    dto.setWrittenReviewCount(reviewRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(user.getId()).size());
                    dto.setProductReviewCount(reviewRepository.findBySellerIdAndIsActiveTrueOrderByCreatedAtDesc(user.getId()).size());
                    dto.setInquiryCount(inquiryRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(user.getId()).size());

                    return dto;
                })
                .toList();
    }

    @Override
    public AdminUserDetailDTO getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        List<AdminOrderDTO> orders = orderRepository.findByBuyerId(userId)
                .stream()
                .map(this::toOrderDTO)
                .toList();

        List<AdminReviewDTO> writtenReviews = reviewRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toReviewDTO)
                .toList();

        List<AdminInquiryDTO> inquiries = inquiryRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toInquiryDTO)
                .toList();

        List<AdminProductDTO> products = List.of();
        List<AdminReviewDTO> productReviews = List.of();

        if (hasRole(user, "SELLER")) {
            products = productRepository.findAll()
                    .stream()
                    .filter(product -> product.getSeller() != null)
                    .filter(product -> Objects.equals(product.getSeller().getId(), userId))
                    .sorted((a, b) -> dateOrMin(b.getCreatedAt()).compareTo(dateOrMin(a.getCreatedAt())))
                    .map(AdminProductDTO::fromEntity)
                    .toList();

            productReviews = reviewRepository.findBySellerIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(this::toReviewDTO)
                    .toList();
        }

        return AdminUserDetailDTO.builder()
                .user(AdminUserDTO.fromEntity(user))
                .orders(orders)
                .writtenReviews(writtenReviews)
                .inquiries(inquiries)
                .products(products)
                .productReviews(productReviews)
                .build();
    }

    @Override
    public List<AdminProductDTO> getProducts() {
        return productRepository.findAll()
                .stream()
                .sorted((a, b) -> dateOrMin(b.getCreatedAt()).compareTo(dateOrMin(a.getCreatedAt())))
                .map(AdminProductDTO::fromEntity)
                .toList();
    }

    @Override
    public List<AdminOrderDTO> getOrders() {
        return orderRepository.findAll()
                .stream()
                .sorted((a, b) -> dateOrMin(b.getCreatedAt()).compareTo(dateOrMin(a.getCreatedAt())))
                .map(this::toOrderDTO)
                .toList();
    }

    @Override
    public List<AdminReviewDTO> getReviews() {
        return reviewRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toReviewDTO)
                .toList();
    }

    @Override
    public List<AdminInquiryDTO> getInquiries() {
        return inquiryRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .sorted((a, b) -> {
                    boolean aPending = "pending".equalsIgnoreCase(a.getStatus());
                    boolean bPending = "pending".equalsIgnoreCase(b.getStatus());

                    if (aPending != bPending) {
                        return aPending ? -1 : 1;
                    }

                    return dateOrMin(b.getCreatedAt()).compareTo(dateOrMin(a.getCreatedAt()));
                })
                .map(this::toInquiryDTO)
                .toList();
    }


    @Override
    @Transactional
    public Map<String, Object> hideReview(Long reviewId) {
        Review review = reviewRepository.findByReviewIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        review.hide();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "리뷰가 숨김 처리되었습니다.");
        result.put("reviewId", reviewId);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> deleteReview(Long reviewId) {
        Review review = reviewRepository.findByReviewIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        review.deactivate();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "리뷰가 삭제 처리되었습니다.");
        result.put("reviewId", reviewId);
        return result;
    }

    private AdminOrderDTO toOrderDTO(Order order) {
        List<AdminOrderItemDTO> items = order.getOrderItems() == null
                ? List.of()
                : order.getOrderItems()
                .stream()
                .map(this::toOrderItemDTO)
                .toList();

        return AdminOrderDTO.fromEntity(
                order,
                getUserName(order.getBuyerId()),
                items
        );
    }

    private AdminOrderItemDTO toOrderItemDTO(OrderItem item) {
        return AdminOrderItemDTO.fromEntity(
                item,
                getProductName(item.getProductId()),
                getUserName(item.getSellerId())
        );
    }

    private AdminReviewDTO toReviewDTO(Review review) {
        Long sellerId = null;

        if (review.getProduct() != null && review.getProduct().getSeller() != null) {
            sellerId = review.getProduct().getSeller().getId();
        }

        return AdminReviewDTO.fromEntity(
                review,
                getUserName(review.getUserId()),
                getUserName(sellerId)
        );
    }

    private AdminInquiryDTO toInquiryDTO(Inquiry inquiry) {
        String role = getUserRole(inquiry.getUserId());

        return AdminInquiryDTO.fromEntity(
                inquiry,
                getUserName(inquiry.getUserId()),
                role,
                getUserRoleText(role)
        );
    }


    private String getUserName(Long userId) {
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
                .map(User::getName)
                .orElse("회원 " + userId);
    }

    private String getProductName(Long productId) {
        if (productId == null) {
            return null;
        }

        return productRepository.findById(productId)
                .map(Product::getName)
                .orElse("상품 " + productId);
    }

    private boolean hasRole(User user, String role) {
        return user != null
                && user.getRole() != null
                && user.getRole().name().equalsIgnoreCase(role);
    }

    private String getUserRole(Long userId) {
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
                .map(User::getRole)
                .map(Enum::name)
                .orElse(null);
    }

    private String getUserRoleText(String role) {
        if (role == null) {
            return "-";
        }

        return switch (role.toUpperCase()) {
            case "BUYER" -> "구매자";
            case "SELLER" -> "판매자";
            case "ADMIN" -> "관리자";
            default -> role;
        };
    }


    private LocalDateTime dateOrMin(LocalDateTime dateTime) {
        return dateTime == null ? LocalDateTime.MIN : dateTime;
    }
}