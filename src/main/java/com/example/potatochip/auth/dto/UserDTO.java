package com.example.potatochip.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDTO {
    private String name;
    private String email;
    private String password;
    private String passwordConfirm;
    private String role;       // "구매자" | "판매자(농가)"
    private String address;
}