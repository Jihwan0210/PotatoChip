package com.example.potatochip.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 로그인 이메일 (고유값)

    @Column(nullable = false)
    private String password; // BCrypt 암호화된 비밀번호

    @Column(nullable = false)
    private String name; // 사용자 실명

    private String address; // 배송 주소 (선택)

    private String phone; // 연락처 (선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 역할 (BUYER / SELLER)

    @Column(nullable = false)
    private Boolean pushAgree; // 푸시 알림 수신 동의

    @Column(nullable = false)
    private Boolean emailAgree; // 이메일 수신 동의

    @Column(nullable = false)
    private Boolean isActive; // 계정 활성화 여부

    private LocalDateTime deletedAt; // 탈퇴 일시

    private String withdrawalReason; // 탈퇴 사유

    private Long deletedBy; // 삭제 처리한 관리자 ID

    private String nickname; // 닉네임 (커뮤니티 표시용)

    @Column(nullable = false)
    private LocalDateTime createdAt; // 가입 일시

    @Column(nullable = false)
    private LocalDateTime updatedAt; // 최종 수정 일시

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}