package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.dto.LoginRequestDTO;
import com.example.potatochip.auth.dto.UserDTO;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.service.AuthService;
import com.example.potatochip.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Controller
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // 로그인 - JWT 토큰 반환
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        boolean success = authService.login(dto);

        if (!success) {
            return ResponseEntity.badRequest().body(Map.of("error", "이메일 또는 비밀번호가 틀렸어요!"));
        }

        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "message", "로그인 성공",
                "token", token,
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }

    // 메인 페이지
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> signup(@RequestBody UserDTO dto) {
        try {
            authService.signup(dto);
            return ResponseEntity.ok(Map.of("message", "회원가입 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}