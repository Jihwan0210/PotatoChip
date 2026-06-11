package com.example.potatochip.admin.controller;


import com.example.potatochip.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        model.addAttribute(
                "dashboard",
                adminService.getDashboard()
        );

        return "admin/dashboard";
    }
}
