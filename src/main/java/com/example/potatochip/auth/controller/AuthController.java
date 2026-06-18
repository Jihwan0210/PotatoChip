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
                "role", user.getRole(),
                "name", user.getName()
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

    // 비밀번호 찾기 - 이메일+이름 검증 후 토큰 발급
    @PostMapping("/forgot-password")
    @ResponseBody
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String token = authService.verifyAndIssueToken(body.get("email"), body.get("name"));
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 이메일 중복 실시간 체크
    @GetMapping("/api/check/email")
    @ResponseBody
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // 비밀번호 형식 실시간 체크
    @PostMapping("/api/check/password")
    @ResponseBody
    public ResponseEntity<?> checkPassword(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        boolean valid = password != null &&
                password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]).{8,}$");
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    // 비밀번호 확인 실시간 체크
    @PostMapping("/api/check/password-confirm")
    @ResponseBody
    public ResponseEntity<?> checkPasswordConfirm(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        String passwordConfirm = body.get("passwordConfirm");
        boolean match = password != null && password.equals(passwordConfirm);
        return ResponseEntity.ok(Map.of("match", match));
    }

    // 비밀번호 재설정 페이지
    @GetMapping("/resetpw")
    public String resetPwPage() {
        return "auth/resetpw";
    }

    // 비밀번호 재설정 처리
    @PostMapping("/resetpw")
    @ResponseBody
    public ResponseEntity<?> resetPw(@RequestBody Map<String, String> body) {
        try {
            authService.resetPassword(body.get("token"), body.get("password"), body.get("passwordConfirm"));
            return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}