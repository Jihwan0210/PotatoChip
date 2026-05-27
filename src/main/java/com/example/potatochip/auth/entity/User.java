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
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String address;

    private String phone;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private Boolean pushAgree;

    @Column(nullable = false)
    private Boolean emailAgree;

    @Column(nullable = false)
    private Boolean isActive;

    private LocalDateTime deletedAt;

    private String withdrawalReason;

    private Long deletedBy;

    private String nickname;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
