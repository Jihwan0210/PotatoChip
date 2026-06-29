package com.example.potatochip.payment.service;

import com.example.potatochip.order.dto.OrderRequestDTO;
import com.example.potatochip.order.service.OrderService;
import com.example.potatochip.payment.dto.PaymentDTO;
import com.example.potatochip.payment.entity.Payment;
import com.example.potatochip.payment.repository.PaymentRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${kakao.pay.admin-key}")  private String adminKey;
    @Value("${kakao.pay.cid}")        private String cid;
    @Value("${kakao.pay.ready-url}")  private String readyUrl;
    @Value("${kakao.pay.approve-url}")private String approveUrl;
    @Value("${kakao.pay.approval-redirect}") private String approvalRedirect;
    @Value("${kakao.pay.cancel-redirect}")   private String cancelRedirect;
    @Value("${kakao.pay.fail-redirect}")     private String failRedirect;

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final RestTemplate restTemplate = new RestTemplate();

    // tid → 주문 생성에 필요한 임시 데이터 저장
    private final Map<String, PendingOrder> pendingStore = new ConcurrentHashMap<>();
    // userId → tid 매핑 (approve 시 userId로 tid를 찾기 위해)
    private final Map<Long, String> userTidStore = new ConcurrentHashMap<>();

    @Getter
    private static class PendingOrder {
        private final Long userId;
        private final String partnerOrderId;
        private final String shippingAddress;
        private final String deliveryType;
        private final List<Long> selectedProductIds;
        private final BigDecimal shippingFee;
        private final int totalAmount;

        PendingOrder(Long userId, String partnerOrderId, String shippingAddress, String deliveryType,
                     List<Long> selectedProductIds, BigDecimal shippingFee, int totalAmount) {
            this.userId = userId;
            this.partnerOrderId = partnerOrderId;
            this.shippingAddress = shippingAddress;
            this.deliveryType = deliveryType;
            this.selectedProductIds = selectedProductIds;
            this.shippingFee = shippingFee;
            this.totalAmount = totalAmount;
        }
    }

    @Override
    public PaymentDTO.ReadyResponse kakaoReady(Long userId, String itemName, int totalAmount,
                                               String shippingAddress, String deliveryType,
                                               List<Long> selectedProductIds, BigDecimal shippingFee,
                                               String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // approve 콜백 URL에 userId와 token을 파라미터로 포함
        String approvalUrl = approvalRedirect
                + "?user_id=" + userId
                + "&token=" + (token != null ? token : "");

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        String partnerOrderId = "order_" + userId + "_" + System.currentTimeMillis();
        body.put("partner_order_id", partnerOrderId);
        body.put("partner_user_id", String.valueOf(userId));
        body.put("item_name", itemName);
        body.put("quantity", 1);
        body.put("total_amount", totalAmount);
        body.put("tax_free_amount", 0);
        body.put("approval_url", approvalUrl);
        body.put("cancel_url", cancelRedirect);  // 취소 → /orders (장바구니로 이동 가능)
        body.put("fail_url", failRedirect);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<PaymentDTO.ReadyResponse> response =
                restTemplate.postForEntity(readyUrl, entity, PaymentDTO.ReadyResponse.class);

        PaymentDTO.ReadyResponse ready = response.getBody();

        // 주문 정보를 tid와 함께 임시 보관 (주문 생성은 approve 때)
        pendingStore.put(ready.getTid(), new PendingOrder(
                userId, partnerOrderId, shippingAddress, deliveryType,
                selectedProductIds, shippingFee, totalAmount
        ));
        userTidStore.put(userId, ready.getTid());

        return ready;
    }

    @Override
    public Long kakaoApprove(String pgToken, Long userId) {
        String tid = userTidStore.get(userId);
        if (tid == null) throw new IllegalStateException("결제 준비 정보가 없습니다. 다시 시도해주세요.");

        PendingOrder pending = pendingStore.get(tid);
        if (pending == null) throw new IllegalStateException("주문 정보가 만료되었습니다.");

        // 1. 카카오페이 승인 요청
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("tid", tid);
        body.put("partner_order_id", pending.getPartnerOrderId());
        body.put("partner_user_id", String.valueOf(userId));
        body.put("pg_token", pgToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(approveUrl, entity, PaymentDTO.ApproveResponse.class);

        // 2. 결제 승인 후 주문 생성
        OrderRequestDTO orderReq = new OrderRequestDTO();
        orderReq.setShippingAddress(pending.getShippingAddress());
        orderReq.setPaymentMethod("kakaopay");
        orderReq.setDeliveryType(pending.getDeliveryType());
        orderReq.setSelectedProductIds(pending.getSelectedProductIds());
        orderReq.setShippingFee(pending.getShippingFee());
        Long orderId = orderService.OrderFromCart(userId, orderReq);

        // 3. 결제 내역 DB 저장
        paymentRepository.save(Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .tid(tid)
                .method("kakaopay")
                .amount(pending.getTotalAmount())
                .status("APPROVED")
                .build());

        // 4. 임시 데이터 정리
        pendingStore.remove(tid);
        userTidStore.remove(userId);

        return orderId;
    }
}