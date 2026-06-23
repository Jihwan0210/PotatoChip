package com.example.potatochip.payment.service;

import com.example.potatochip.order.service.OrderService;
import com.example.potatochip.payment.dto.PaymentDTO;
import com.example.potatochip.payment.entity.Payment;
import com.example.potatochip.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${kakao.pay.admin-key}")
    private String adminKey;

    @Value("${kakao.pay.cid}")
    private String cid;

    @Value("${kakao.pay.ready-url}")
    private String readyUrl;

    @Value("${kakao.pay.approve-url}")
    private String approveUrl;

    @Value("${kakao.pay.approval-redirect}")
    private String approvalRedirect;

    @Value("${kakao.pay.cancel-redirect}")
    private String cancelRedirect;

    @Value("${kakao.pay.fail-redirect}")
    private String failRedirect;

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // tid 임시 저장
    public final Map<Long, String> tidStore = new ConcurrentHashMap<>();

    @Override
    public PaymentDTO.ReadyResponse kakaoReady(Long orderId, String itemName,
                                               int totalAmount, Long userId,
                                               String shippingAddress, String deliveryType,
                                                String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("partner_order_id", String.valueOf(orderId));
        body.put("partner_user_id", String.valueOf(userId));
        body.put("item_name", itemName);
        body.put("quantity", 1);
        body.put("total_amount", totalAmount);
        body.put("tax_free_amount", 0);
        body.put("approval_url", approvalRedirect + "?order_id=" + orderId
                + "&token=" + (token != null ? token : ""));
        body.put("cancel_url", cancelRedirect);
        body.put("fail_url", failRedirect);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<PaymentDTO.ReadyResponse> response =
                restTemplate.postForEntity(readyUrl, entity, PaymentDTO.ReadyResponse.class);

        PaymentDTO.ReadyResponse ready = response.getBody();

        // DB에 READY 상태로 저장
        paymentRepository.save(Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .tid(ready.getTid())
                .method("kakaopay")
                .amount(totalAmount)
                .status("READY")
                .build());

        tidStore.put(orderId, ready.getTid());
        return ready;
    }

    @Override
    public PaymentDTO.ApproveResponse kakaoApprove(String pgToken, Long orderId, Long userId) {
        String tid = tidStore.get(orderId);
        if (tid == null) throw new IllegalStateException("tid 없음 - 결제 준비 먼저 필요");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("tid", tid);
        body.put("partner_order_id", String.valueOf(orderId));
        body.put("partner_user_id", String.valueOf(userId));
        body.put("pg_token", pgToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<PaymentDTO.ApproveResponse> response =
                restTemplate.postForEntity(approveUrl, entity, PaymentDTO.ApproveResponse.class);

        // DB 상태 APPROVED로 업데이트
        paymentRepository.findByOrderId(orderId).ifPresent(p -> {
            p.setStatus("APPROVED");
            paymentRepository.save(p);
        });

        tidStore.remove(orderId);
        return response.getBody();
    }
}