package com.example.potatochip.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

public class PaymentDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReadyResponse {
        private String tid;
        private String next_redirect_pc_url;
        private String next_redirect_mobile_url;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ApproveResponse {
        private String aid;
        private String tid;
        private String partner_order_id;
        private String partner_user_id;
        private String payment_method_type;
        private int total;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReadyRequest {
        private String shippingAddress;
        private String deliveryType;
        private String itemName;
        private int totalAmount;
        private List<Long> selectedProductIds;
        private String token;
        private BigDecimal shippingFee;
    }
}