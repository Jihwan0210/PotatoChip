package com.example.potatochip.traking.repository;

import com.example.potatochip.traking.entity.DeliveryTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {

    // 주문 1건 = 추적 레코드 1건이므로 orderId로 단건 조회
    Optional<DeliveryTracking> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);
}