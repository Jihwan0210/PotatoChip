package com.example.potatochip.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class BoardSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain boardFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/board/**") // ★ 중요: 게시판 관련 요청만 이 설정이 처리합니다!
                .csrf(csrf -> csrf.disable()) // 여기서 CSRF를 꺼서 403 에러 해결
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}