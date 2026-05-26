package com.example.potatochip.auth.controller;

import com.example.potatochip.auth.dto.LoginRequestDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(LoginRequestDto dto, Model model) {
        if ("admin".equals(dto.getUsername()) && "1234".equals(dto.getPassword())) {
            return "redirect:/";
        }
        model.addAttribute("error", "아이디 또는 비밀번호가 틀렸어요!");
        return "login";
    }
}