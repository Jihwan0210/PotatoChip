package com.example.potatochip.auth.controller;


import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class MyPageController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping("/mypage/info")
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(java.util.Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "nickname", user.getNickname() != null ? user.getNickname() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "address", user.getAddress() != null ? user.getAddress() : "",
                "role", user.getRole()
        ));
    }
}