package com.example.potatochip.payment.service;

import com.example.potatochip.payment.dto.PaymentDTO;

public interface PaymentService {
    PaymentDTO.ReadyResponse kakaoReady(Long orderId, String itemName, int totalAmount,
                                        Long userId, String shippingAddress,
                                        String deliveryType, String token);
    PaymentDTO.ApproveResponse kakaoApprove(String pgToken, Long orderId, Long userId);
}