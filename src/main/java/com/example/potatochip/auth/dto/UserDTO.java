package com.example.potatochip.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDTO {
    /** 사용자 이름 (실명) */
    private String name;

    /** 사용자 역할 (BUYER: 구매자, SELLER: 판매자) */
    private String role;

    /** 로그인에 사용되는 이메일 (고유값) */
    private String email;

    /** 비밀번호 (암호화 전 원문, 서비스 레이어에서 BCrypt 인코딩) */
    private String password;

    /** 비밀번호 확인 (회원가입 시 password와 일치 여부 검증용) */
    private String passwordConfirm;

    /** 연락처 (선택 입력) */
    private String phone;

    /** 배송 주소 (선택 입력) */
    private String address;

    /** 닉네임 (선택 입력, 커뮤니티 등에서 표시되는 이름) */
    private String nickname;
}