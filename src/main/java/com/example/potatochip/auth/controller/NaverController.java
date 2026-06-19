package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.auth.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class NaverController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Value("${naver.client-id:NAVER_CLIENT_ID_HERE}")
    private String naverClientId;

    @Value("${naver.client-secret:NAVER_CLIENT_SECRET_HERE}")
    private String naverClientSecret;

    @Value("${naver.redirect-uri:http://localhost:8080/oauth2/callback/naver}")
    private String naverRedirectUri;

    @GetMapping("/oauth2/naver")
    public void naverLogin(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        String url = "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + naverClientId
                + "&redirect_uri=" + URLEncoder.encode(naverRedirectUri, StandardCharsets.UTF_8)
                + "&state=" + state;
        response.sendRedirect(url);
    }

    @GetMapping("/oauth2/callback/naver")
    public String naverCallback(@RequestParam String code,
                                @RequestParam(required = false) String state) throws IOException {
        RestTemplate rt = new RestTemplate();

        // 1. 액세스 토큰 교환
        String tokenUrl = "https://nid.naver.com/oauth2.0/token"
                + "?grant_type=authorization_code"
                + "&client_id=" + naverClientId
                + "&client_secret=" + naverClientSecret
                + "&code=" + code
                + "&state=" + (state != null ? state : "");

        ResponseEntity<String> tokenRes = rt.getForEntity(tokenUrl, String.class);
        JsonNode tokenJson = objectMapper.readTree(tokenRes.getBody());
        String accessToken = tokenJson.get("access_token").asText();

        // 2. 사용자 정보 조회
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        ResponseEntity<String> userRes = rt.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class
        );
        JsonNode userJson = objectMapper.readTree(userRes.getBody());
        JsonNode res      = userJson.path("response");
        String email    = res.path("email").asText(res.path("id").asText() + "@naver.local");
        String name     = res.path("name").asText("네이버사용자");
        String nickname = res.path("nickname").asText(null);
        String phone    = res.path("mobile").asText(null);

        return SocialLoginUtil.buildResult(email, name, nickname, phone, userRepository, jwtUtil);
    }
}
