package com.example.potatochip.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDTO {
    private String name;
    private String role;
    private String email;
    private String password;
    private String passwordConfirm;
    private String phone;
    private String address;
    private String nickname;
}