package com.example.potatochip.admin.controller;

import com.example.potatochip.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/admin/dashboard")
    public String adminDashboardPage() {
        return "admin/dashboard";
    }

    @GetMapping("/api/admin/dashboard/summary")
    @ResponseBody
    public Object getSummary() {
        return adminDashboardService.getSummary();
    }

    @GetMapping("/api/admin/dashboard/buyers")
    @ResponseBody
    public Object getBuyers() {
        return adminDashboardService.getBuyers();
    }

    @GetMapping("/api/admin/dashboard/sellers")
    @ResponseBody
    public Object getSellers() {
        return adminDashboardService.getSellers();
    }

    @GetMapping("/api/admin/dashboard/users/{userId}")
    @ResponseBody
    public Object getUserDetail(@PathVariable Long userId) {
        return adminDashboardService.getUserDetail(userId);
    }

    @GetMapping("/api/admin/dashboard/products")
    @ResponseBody
    public Object getProducts() {
        return adminDashboardService.getProducts();
    }

    @GetMapping("/api/admin/dashboard/orders")
    @ResponseBody
    public Object getOrders() {
        return adminDashboardService.getOrders();
    }

    @GetMapping("/api/admin/dashboard/reviews")
    @ResponseBody
    public Object getReviews() {
        return adminDashboardService.getReviews();
    }

    @GetMapping("/api/admin/dashboard/inquiries")
    @ResponseBody
    public Object getInquiries() {
        return adminDashboardService.getInquiries();
    }

    @PatchMapping("/api/admin/dashboard/reviews/{reviewId}/hide")
    @ResponseBody
    public Object hideReview(@PathVariable Long reviewId) {
        return adminDashboardService.hideReview(reviewId);
    }

    @DeleteMapping("/api/admin/dashboard/reviews/{reviewId}")
    @ResponseBody
    public Object deleteReview(@PathVariable Long reviewId) {
        return adminDashboardService.deleteReview(reviewId);
    }
}