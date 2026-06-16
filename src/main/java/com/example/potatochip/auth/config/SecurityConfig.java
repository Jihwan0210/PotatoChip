package com.example.potatochip.auth.config;

import com.example.potatochip.auth.filter.JwtFilter;
import com.example.potatochip.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; //게시글 생성 및 삭제도 로그인 필요
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    // 비밀번호 암호화 - BCrypt 방식
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // JWT 사용으로 CSRF 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 미사용 (JWT 기반)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/signup",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST,"/api/board")
                        .authenticated()
                        // 인증 없이 허용 //->로그인 토큰이 있어야만 가능
                        .requestMatchers(HttpMethod.DELETE,"/api/board/**")
                        .authenticated() //삭제 API도 로그인 필수
                        .requestMatchers(HttpMethod.PUT,"/api/board/**")
                        .authenticated() //게시글 수정

                        .anyRequest().permitAll() // 나머지도 허용 (추후 인증 필요 시 authenticated()로 변경)
                )
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class); // JWT 필터 등록

        return http.build();
    }
}