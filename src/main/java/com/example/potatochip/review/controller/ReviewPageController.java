package com.example.potatochip.review.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReviewPageController {

    @GetMapping("/review")
    public String reviewPage() {
        return "review";
    }
}