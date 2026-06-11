package com.example.potatochip.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FarmerController {

    @GetMapping("/farmer")
    public String farmer() {
        return "auth/farmer";
    }
}