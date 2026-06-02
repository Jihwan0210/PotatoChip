package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.dto.UserDTO;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class MyPageController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 마이페이지 뷰 반환
    @GetMapping("/mypage")
    public String myPage() {
        return "auth/mypage";
    }

    // 내 정보 조회
    @GetMapping("/mypage/info")
    @ResponseBody
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(java.util.Map.of(
                "name",    user.getName(),
                "email",   user.getEmail(),
                "nickname", user.getNickname() != null ? user.getNickname() : "",
                "phone",   user.getPhone()    != null ? user.getPhone()    : "",
                "address", user.getAddress()  != null ? user.getAddress()  : "",
                "role",    user.getRole()
        ));
    }

    // 내 정보 수정
    @PutMapping("/mypage/info")
    @ResponseBody
    public ResponseEntity<?> updateMyInfo(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody UserDTO dto) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = jwtUtil.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();

        if (dto.getName()     != null) user.setName(dto.getName());
        if (dto.getNickname() != null) user.setNickname(dto.getNickname()); //고침
        if (dto.getPhone()    != null) user.setPhone(dto.getPhone());
        if (dto.getAddress()  != null) user.setAddress(dto.getAddress());

        userRepository.save(user);

        return ResponseEntity.ok(java.util.Map.of("message", "수정 완료"));
    }
}