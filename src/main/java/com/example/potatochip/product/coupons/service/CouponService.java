package com.example.potatochip.product.coupons.service;

import com.example.potatochip.product.coupons.dto.CouponCreateDTO;
import com.example.potatochip.product.coupons.dto.CouponResponseDTO;
import com.example.potatochip.product.coupons.dto.UserCouponDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    void createCoupon(CouponCreateDTO dto, Long createdById, String role);
    void deactivateCoupon(Long couponId, Long requesterId, String requesterRole);
    void registerCoupon(String code, Long userId);
    List<UserCouponDTO> getAvailableCoupons(Long userId);
    BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount, BigDecimal shippingFee);
    BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount, BigDecimal shippingFee, java.util.List<Long> orderedProductIds);
    BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount, BigDecimal shippingFee, java.util.List<Long> orderedProductIds, BigDecimal targetProductAmount);
    void useCoupon(Long userCouponId);
    List<CouponResponseDTO> getMyCoupons(Long createdById, String role);
    void createProductCoupon(Long productId, CouponCreateDTO dto, Long sellerId);
    UserCouponDTO getProductCoupon(Long productId);
    void downloadCoupon(Long couponId, Long userId);
    CouponResponseDTO getSellerProductCoupon(Long productId, Long sellerId);
    Long getCouponTargetProductId(Long userCouponId);
}