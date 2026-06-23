package com.example.potatochip.auth.service;

import com.example.potatochip.auth.dto.LoginRequestDTO;
import com.example.potatochip.auth.dto.UserDTO;
import com.example.potatochip.auth.entity.ResetPw;
import com.example.potatochip.auth.entity.Role;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.ResetPwRepository;
import com.example.potatochip.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResetPwRepository resetPwRepository;

    // 로그인
    public boolean login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElse(null);

        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) return false;

        return passwordEncoder.matches(dto.getPassword(), user.getPassword());
    }

    // 회원가입
    public void signup(UserDTO dto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");

        // 비밀번호 확인
        if (!dto.getPassword().equals(dto.getPasswordConfirm()))
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        Role role = "판매자(농가)".equals(dto.getRole()) ? Role.SELLER : Role.BUYER;

        User user = new User();
        user.setName(dto.getName());
        user.setRole(role);
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setNickname(dto.getNickname());
        user.setPushAgree(false);
        user.setEmailAgree(false);
        user.setIsActive(true);

        userRepository.save(user);
    }

    // 이메일 + 이름으로 본인 확인 후 재설정 토큰 발급
    @Transactional
    public String verifyAndIssueToken(String email, String name) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입된 이메일이 없습니다."));

        if (!user.getName().equals(name)) {
            throw new IllegalArgumentException("이름이 일치하지 않습니다.");
        }

        // 기존 토큰 삭제 후 새로 발급
        resetPwRepository.deleteByUser_Id(user.getId());

        ResetPw resetPw = new ResetPw();
        resetPw.setToken(UUID.randomUUID().toString());
        resetPw.setUser(user);
        resetPw.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        resetPwRepository.save(resetPw);

        return resetPw.getToken();
    }

    // 토큰 검증 후 비밀번호 변경
    @Transactional
    public void resetPassword(String token, String newPassword, String passwordConfirm) {
        if (!newPassword.equals(passwordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        ResetPw resetPw = resetPwRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 링크입니다."));

        if (resetPw.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetPwRepository.delete(resetPw);
            throw new IllegalArgumentException("만료된 링크입니다. 다시 시도해주세요.");
        }

        User user = resetPw.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetPwRepository.delete(resetPw);
    }
}