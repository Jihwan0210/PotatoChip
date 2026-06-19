package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.entity.Role;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

class SocialLoginUtil {

    // 카카오용 (nickname, phone 없음)
    static String buildResult(String email, String name,
                              UserRepository userRepository, JwtUtil jwtUtil) {
        return buildResult(email, name, null, null, userRepository, jwtUtil);
    }

    // 네이버용 (nickname, phone 포함)
    static String buildResult(String email, String name, String nickname, String phone,
                              UserRepository userRepository, JwtUtil jwtUtil) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPassword("SOCIAL_" + UUID.randomUUID());
            newUser.setRole(Role.BUYER);
            newUser.setIsActive(true);
            newUser.setPushAgree(false);
            newUser.setEmailAgree(false);
            return newUser;
        });

        // 이름 업데이트 (기존 유저도 최신 소셜 정보로 갱신)
        user.setName(name);
        if (nickname != null && !nickname.isBlank()) user.setNickname(nickname);
        if (phone    != null && !phone.isBlank())    user.setPhone(phone);
        userRepository.save(user);

        String jwt          = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String encodedName  = URLEncoder.encode(user.getName(),  StandardCharsets.UTF_8);
        String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);

        return "redirect:/login?oauth_token=" + jwt
                + "&oauth_name="  + encodedName
                + "&oauth_email=" + encodedEmail
                + "&oauth_role="  + user.getRole().name();
    }
}
