package com.example.potatochip.payment.service;

import com.example.potatochip.payment.dto.PaymentDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {


    PaymentDTO.ReadyResponse kakaoReady(Long userId, String itemName, int totalAmount,
                                        String shippingAddress, String deliveryType,
                                        List<Long> selectedProductIds, BigDecimal shippingFee,
                                        String token);

    Long kakaoApprove(String pgToken, Long userId);
}