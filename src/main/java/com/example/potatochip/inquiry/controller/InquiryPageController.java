package com.example.potatochip.inquiry.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InquiryPageController {

    @GetMapping("/inquiries/create")
    public String createInquiryPage() {
        return "inquiry/create";
    }

    @GetMapping("/inquiries/my")
    public String myInquiriesPage() {
        return "inquiry/my";
    }

    @GetMapping("/admin/inquiries")
    public String adminInquiriesPage() {
        return "admin/inquiries";
    }
}