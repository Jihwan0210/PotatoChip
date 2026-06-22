package com.example.potatochip.auth.config;

import com.example.potatochip.auth.filter.JwtFilter;
import com.example.potatochip.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        // 1. 누구나 접근 가능한 정적 자원 및 인증 API (+ 업로드된 이미지 경로 추가)
                        .requestMatchers("/signup", "/login", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()

                        // 2. 관리자 전용 경로 - ADMIN 역할만 접근 가능
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // 3. 문의사항(Inquiry) 관련 권한 인증 설정 (develop 변경사항 반영)
                        .requestMatchers("/api/inquiries/**").authenticated() // 로그인시 문의 허용

                        // 4. 게시판(Board) 관련 권한 인증 설정 (feature/Operations 변경사항 반영)
                        .requestMatchers(HttpMethod.POST, "/api/board").authenticated()     // 게시글 생성 시 로그인 필수
                        .requestMatchers(HttpMethod.PUT, "/api/board/**").authenticated()    // 게시글 수정 시 로그인 필수
                        .requestMatchers(HttpMethod.DELETE, "/api/board/**").authenticated() // 게시글 삭제 시 로그인 필수

                        // 5. WebSocket 핸드셰이크 허용
                        .requestMatchers("/ws-chat/**").permitAll()

                        // 6. 채팅 API 인증 필요
                        .requestMatchers("/chat/**").authenticated()

                        // 7. faq curd 관리자권한
                        .requestMatchers(HttpMethod.POST, "/api/ai/chatbot/faqs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/ai/chatbot/faqs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/ai/chatbot/faqs/**").hasRole("ADMIN")

                        // 8. 그 외 나머지 요청은 우선 허용
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/order-items/**").authenticated()

                        // 9. 그 외 나머지 요청은 우선 허용
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class); // JWT 필터 등록

        return http.build();
    }
}