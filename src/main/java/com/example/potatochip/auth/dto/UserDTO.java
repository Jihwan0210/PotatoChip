package com.example.potatochip.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDTO {
    //사용자 이름 
    private String name;

    //사용자 역할 (BUYER: 구매자, SELLER: 판매자)
    private String role;

    //로그인에 사용되는 이메일
    private String email;

    //비밀번호 (암호화)
    private String password;

    // 비밀번호 확인 
    private String passwordConfirm;

    // 연락처
    private String phone;

    // 배송 주소 (선택 입력)
    private String address;

    // 닉네임 (선택 입력, 커뮤니티 등에서 표시되는 이름)
    private String nickname;
}