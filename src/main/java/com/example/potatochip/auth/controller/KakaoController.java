package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.entity.Role;
import com.example.potatochip.auth.entity.User;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class KakaoController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Value("${kakao.client-id:KAKAO_CLIENT_ID_HERE}")
    private String kakaoClientId;

    @Value("${kakao.client-secret:}")
    private String kakaoClientSecret;

    @Value("${kakao.redirect-uri:http://localhost:8080/oauth2/callback/kakao}")
    private String kakaoRedirectUri;

    @GetMapping("/oauth2/kakao")
    public String kakaoLogin() {
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + URLEncoder.encode(kakaoRedirectUri, StandardCharsets.UTF_8)
                + "&prompt=login";
        return "redirect:" + url;
    }

    @GetMapping("/oauth2/callback/kakao")
    public String kakaoCallback(@RequestParam String code) throws IOException {
        RestTemplate rt = new RestTemplate();

        // 1. 액세스 토큰 교환
        String body = "grant_type=authorization_code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + URLEncoder.encode(kakaoRedirectUri, StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + (kakaoClientSecret != null && !kakaoClientSecret.isEmpty()
                    ? "&client_secret=" + kakaoClientSecret : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        ResponseEntity<String> tokenRes = rt.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                new HttpEntity<>(body, headers),
                String.class
        );
        JsonNode tokenJson = objectMapper.readTree(tokenRes.getBody());
        String accessToken = tokenJson.get("access_token").asText();

        // 2. 사용자 정보 조회
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        ResponseEntity<String> userRes = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class
        );
        JsonNode userJson     = objectMapper.readTree(userRes.getBody());
        String kakaoId        = userJson.get("id").asText();
        JsonNode kakaoAccount = userJson.path("kakao_account");
        String email = kakaoAccount.path("email").asText(kakaoId + "@kakao.local");
        String name  = kakaoAccount.path("profile").path("nickname").asText("카카오사용자");

        return SocialLoginUtil.buildResult(email, name, userRepository, jwtUtil);
    }
}
