package com.example.potatochip.product.coupons.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.coupons.dto.CouponCreateDTO;
import com.example.potatochip.product.coupons.dto.CouponResponseDTO;
import com.example.potatochip.product.coupons.dto.UserCouponDTO;
import com.example.potatochip.product.coupons.entity.Coupon;
import com.example.potatochip.product.coupons.entity.DiscountType;
import com.example.potatochip.product.coupons.entity.UserCoupon;
import com.example.potatochip.product.coupons.repository.CouponRepository;
import com.example.potatochip.product.coupons.repository.UserCouponRepository;
import com.example.potatochip.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void createCoupon(CouponCreateDTO dto, Long createdById, String role) {
        if (couponRepository.existsByCode(dto.getCode().toUpperCase())) {
            throw new IllegalArgumentException("이미 존재하는 쿠폰 코드입니다.");
        }
        Coupon coupon = Coupon.builder()
                .code(dto.getCode().toUpperCase())
                .name(dto.getName())
                .discountType(DiscountType.valueOf(dto.getDiscountType()))
                .discountValue(dto.getDiscountValue())
                .minOrderAmount(dto.getMinOrderAmount())
                .maxDiscountAmount(dto.getMaxDiscountAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .maxUsage(dto.getMaxUsage())
                .productId(dto.getProductId())
                .createdById(createdById)
                .createdByRole(role)
                .build();
        couponRepository.save(coupon);
    }

    @Override
    @Transactional
    public void deactivateCoupon(Long couponId, Long requesterId, String requesterRole) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        if (!coupon.getCreatedById().equals(requesterId)) {
            throw new IllegalArgumentException("본인이 만든 쿠폰만 비활성화할 수 있습니다.");
        }
        coupon.setIsActive(false);
        couponRepository.save(coupon);
    }

    @Override
    @Transactional
    public void registerCoupon(String code, Long userId) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰 코드입니다."));

        LocalDateTime now = LocalDateTime.now();
        if (!coupon.getIsActive() || now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
            throw new IllegalArgumentException("사용할 수 없는 쿠폰입니다.");
        }
        if (coupon.getMaxUsage() != null && coupon.getCurrentUsage() >= coupon.getMaxUsage()) {
            throw new IllegalArgumentException("쿠폰 발급 한도가 초과되었습니다.");
        }
        if (userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())) {
            throw new IllegalArgumentException("이미 등록된 쿠폰입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        userCouponRepository.save(UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .isUsed(false)
                .build());
    }

    @Override
    public List<UserCouponDTO> getAvailableCoupons(Long userId) {
        return userCouponRepository.findAvailableCoupons(userId, LocalDateTime.now())
                .stream()
                .map(uc -> {
                    Coupon c = uc.getCoupon();
                    String productName = null;
                    if (c.getProductId() != null) {
                        productName = productRepository.findById(c.getProductId())
                                .map(p -> p.getName())
                                .orElse(null);
                    }
                    return UserCouponDTO.builder()
                            .userCouponId(uc.getId())
                            .couponId(c.getId())
                            .code(c.getCode())
                            .name(c.getName())
                            .discountType(c.getDiscountType().name())
                            .discountValue(c.getDiscountValue())
                            .minOrderAmount(c.getMinOrderAmount())
                            .maxDiscountAmount(c.getMaxDiscountAmount())
                            .endDate(c.getEndDate())
                            .targetProductId(c.getProductId())
                            .targetProductName(productName)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount, BigDecimal shippingFee) {
        return calculateDiscount(userCouponId, orderAmount, shippingFee, null, null);
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount, BigDecimal shippingFee,
                                        java.util.List<Long> orderedProductIds) {
        return calculateDiscount(userCouponId, orderAmount, shippingFee, orderedProductIds, null);
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount, BigDecimal shippingFee,
                                        java.util.List<Long> orderedProductIds, BigDecimal targetProductAmount) {
        UserCoupon uc = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        if (uc.getIsUsed()) throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");

        Coupon coupon = uc.getCoupon();

        if (coupon.getProductId() != null && orderedProductIds != null
                && !orderedProductIds.contains(coupon.getProductId())) {
            throw new IllegalArgumentException("이 쿠폰은 특정 상품에만 사용할 수 있습니다.");
        }

        // minOrderAmount는 전체 주문금액 기준으로 검사
        if (coupon.getMinOrderAmount() != null
                && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException(
                    "최소 주문 금액 조건을 충족하지 못했습니다. (최소: "
                            + coupon.getMinOrderAmount().toPlainString() + "원)");
        }

        // 상품 전용 쿠폰이면 해당 상품 금액만, 아니면 전체 금액에 적용
        BigDecimal baseAmount = (coupon.getProductId() != null && targetProductAmount != null)
                ? targetProductAmount : orderAmount;

        return switch (coupon.getDiscountType()) {
            case FREE_SHIPPING -> shippingFee != null ? shippingFee : BigDecimal.ZERO;
            case FIXED_AMOUNT  -> coupon.getDiscountValue().min(baseAmount);
            case PERCENTAGE -> {
                BigDecimal discount = baseAmount
                        .multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
                if (coupon.getMaxDiscountAmount() != null) {
                    discount = discount.min(coupon.getMaxDiscountAmount());
                }
                yield discount;
            }
        };
    }

    @Override
    @Transactional
    public void createProductCoupon(Long productId, CouponCreateDTO dto, Long sellerId) {
        // 기존 활성 쿠폰 비활성화
        couponRepository.findFirstByProductIdAndIsActiveTrueOrderByCreatedAtDesc(productId)
                .ifPresent(old -> { old.setIsActive(false); couponRepository.save(old); });

        String autoCode = "PROD" + productId + "_" + System.currentTimeMillis();
        Coupon coupon = Coupon.builder()
                .code(autoCode)
                .name(dto.getName())
                .discountType(DiscountType.valueOf(dto.getDiscountType()))
                .discountValue(dto.getDiscountValue())
                .minOrderAmount(dto.getMinOrderAmount())
                .maxDiscountAmount(dto.getMaxDiscountAmount())
                .startDate(dto.getStartDate() != null ? dto.getStartDate() : java.time.LocalDateTime.now())
                .endDate(dto.getEndDate())
                .isActive(true)
                .maxUsage(dto.getMaxUsage())
                .currentUsage(0)
                .productId(productId)
                .createdById(sellerId)
                .createdByRole("SELLER")
                .build();
        couponRepository.save(coupon);
    }

    @Override
    public UserCouponDTO getProductCoupon(Long productId) {
        return couponRepository.findFirstByProductIdAndIsActiveTrueAndEndDateAfterOrderByCreatedAtDesc(productId, LocalDateTime.now())
                .map(c -> {
                    String productName = productRepository.findById(productId)
                            .map(p -> p.getName()).orElse(null);
                    return UserCouponDTO.builder()
                            .couponId(c.getId())
                            .code(c.getCode())
                            .name(c.getName())
                            .discountType(c.getDiscountType().name())
                            .discountValue(c.getDiscountValue())
                            .minOrderAmount(c.getMinOrderAmount())
                            .maxDiscountAmount(c.getMaxDiscountAmount())
                            .endDate(c.getEndDate())
                            .targetProductId(productId)
                            .targetProductName(productName)
                            .build();
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public void downloadCoupon(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        if (!coupon.getIsActive()) throw new IllegalArgumentException("비활성화된 쿠폰입니다.");
        if (java.time.LocalDateTime.now().isAfter(coupon.getEndDate()))
            throw new IllegalArgumentException("만료된 쿠폰입니다.");
        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId))
            throw new IllegalArgumentException("이미 다운로드한 쿠폰입니다.");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        userCouponRepository.save(UserCoupon.builder()
                .user(user).coupon(coupon).isUsed(false).build());
    }

    @Override
    public Long getCouponTargetProductId(Long userCouponId) {
        return userCouponRepository.findById(userCouponId)
                .map(uc -> uc.getCoupon().getProductId())
                .orElse(null);
    }

    @Override
    public CouponResponseDTO getSellerProductCoupon(Long productId, Long sellerId) {
        return couponRepository.findFirstByProductIdAndCreatedByIdOrderByCreatedAtDesc(productId, sellerId)
                .map(c -> {
                    String productName = productRepository.findById(productId)
                            .map(p -> p.getName()).orElse(null);
                    return CouponResponseDTO.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .discountType(c.getDiscountType().name())
                            .discountValue(c.getDiscountValue())
                            .minOrderAmount(c.getMinOrderAmount())
                            .maxDiscountAmount(c.getMaxDiscountAmount())
                            .startDate(c.getStartDate())
                            .endDate(c.getEndDate())
                            .isActive(c.getIsActive())
                            .currentUsage(c.getCurrentUsage())
                            .productId(productId)
                            .targetProductName(productName)
                            .createdAt(c.getCreatedAt())
                            .build();
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public void useCoupon(Long userCouponId) {
        UserCoupon uc = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        if (uc.getIsUsed()) {
            throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
        }
        uc.setIsUsed(true);
        uc.setUsedAt(LocalDateTime.now());
        userCouponRepository.save(uc);

        Coupon coupon = uc.getCoupon();
        coupon.setCurrentUsage(coupon.getCurrentUsage() + 1);
        couponRepository.save(coupon);
    }

    @Override
    public List<CouponResponseDTO> getMyCoupons(Long createdById, String role) {
        return couponRepository.findByCreatedByIdAndCreatedByRole(createdById, role)
                .stream()
                .map(c -> {
                    String productName = null;
                    if (c.getProductId() != null) {
                        productName = productRepository.findById(c.getProductId())
                                .map(p -> p.getName()).orElse(null);
                    }
                    return CouponResponseDTO.builder()
                            .id(c.getId())
                            .code(c.getCode())
                            .name(c.getName())
                            .discountType(c.getDiscountType().name())
                            .discountValue(c.getDiscountValue())
                            .minOrderAmount(c.getMinOrderAmount())
                            .maxDiscountAmount(c.getMaxDiscountAmount())
                            .startDate(c.getStartDate())
                            .endDate(c.getEndDate())
                            .isActive(c.getIsActive())
                            .maxUsage(c.getMaxUsage())
                            .currentUsage(c.getCurrentUsage())
                            .productId(c.getProductId())
                            .targetProductName(productName)
                            .createdAt(c.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}