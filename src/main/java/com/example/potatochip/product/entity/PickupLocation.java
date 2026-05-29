package com.example.potatochip.product.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "pickup_locations")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PickupLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //픽업 장소 ID

    @Column(name = "seller_id" , nullable = false)
    private Long seller_id; // 판매자 ID

    private String address; // 주소


    private BigDecimal latitude; // 위도 (카카오맵)


    private BigDecimal longitude; // 경도 (카카오맵)


    private String operatingHours; //운영 시간

    @Column(nullable = false)
    private Boolean isActive; // 활성 상태

    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성 일시

    @Column(nullable = false)
    private LocalDateTime updatedAt; //수정 일시
}
