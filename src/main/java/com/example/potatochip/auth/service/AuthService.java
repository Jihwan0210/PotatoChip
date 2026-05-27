package com.example.potatochip.auth.service;

import com.example.potatochip.auth.dto.LoginRequestDTO;
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

    public boolean login(LoginRequestDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElse(null);

        if (user == null) {
            return false;
        }

        return passwordEncoder.matches(dto.getPassword(), user.getPassword());
    }
}