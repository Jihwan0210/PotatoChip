package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.dto.LoginRequestDTO;
import com.example.potatochip.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(LoginRequestDTO dto, Model model) {

        boolean success = authService.login(dto);

        if (success) {
            return "redirect:/";
        }

        model.addAttribute("error", "아이디 또는 비밀번호가 틀렸어요!");
        return "login";
    }
}