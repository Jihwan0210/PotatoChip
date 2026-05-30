package com.example.potatochip.ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatbotFaqController {

    @GetMapping("/faq")
    public String faqPage() {
        return "faq/faq";
    }
}