package com.example.potatochip.auth.service;

import com.example.potatochip.auth.dto.LoginRequestDTO;
import com.example.potatochip.auth.dto.UserDTO;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 로그인 (기존 유지)
    public boolean login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElse(null);

        if (user == null) return false;

        return passwordEncoder.matches(dto.getPassword(), user.getPassword());
    }

    // 회원가입 (UserDTO로 변경)
    public void signup(UserDTO dto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");

        // 비밀번호 확인
        if (!dto.getPassword().equals(dto.getPasswordConfirm()))
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        String role = "판매자(농가)".equals(dto.getRole()) ? "SELLER" : "BUYER";

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setAddress(dto.getAddress());
        user.setRole(role);
        user.setPushAgree(false);
        user.setEmailAgree(false);
        user.setIsActive(true);

        userRepository.save(user);
    }
}