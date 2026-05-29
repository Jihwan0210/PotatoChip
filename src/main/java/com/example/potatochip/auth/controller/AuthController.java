package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.dto.LoginRequestDTO;
import com.example.potatochip.auth.dto.UserDTO;
import com.example.potatochip.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Controller
public class AuthController {

    private final AuthService authService;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(LoginRequestDTO dto, Model model) {
        boolean success = authService.login(dto);

        if (success) {
            return "redirect:/";
        }

        model.addAttribute("error", "이메일 또는 비밀번호가 틀렸어요!");
        return "auth/login";
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