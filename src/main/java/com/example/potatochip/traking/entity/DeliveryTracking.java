package com.example.potatochip.traking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주문 1건의 "현재" 배송 위치를 저장하는 테이블.
 * 배송 상태(준비중/배송중/완료)는 Order.status를 그대로 사용하고,
 * 여기서는 좌표와 마지막 갱신 시각만 책임진다.
 *
 * 기사 앱(또는 디바이스)이 주기적으로 PUT 요청을 보내 위치를 갱신하고,
 * 구매자 화면은 이 테이블을 polling 하거나 마지막 값을 1회 조회한다.
 */
@Entity
@Table(name = "delivery_tracking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1건의 주문은 1개의 배송 추적 레코드만 가진다 (1:1)
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    // 현재 위도 / 경도
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // 배송 기사 이름 / 연락처 (선택 정보, 화면에 보여주기용)
    @Column(name = "driver_name", length = 50)
    private String driverName;

    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    // 도착 예정 시각 (택배사 API 연동 시 채워질 값, 지금은 null 허용)
    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 기사 앱에서 좌표가 들어올 때마다 호출.
     * updatedAt은 @PreUpdate가 자동으로 갱신해준다.
     */
    public void updateLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}