package com.example.potatochip.admin.service;

import com.example.potatochip.admin.dto.*;

import java.util.List;
import java.util.Map;

public interface AdminDashboardService {

    AdminSummaryDTO getSummary();

    List<AdminUserDTO> getBuyers();

    List<AdminUserDTO> getSellers();

    AdminUserDetailDTO getUserDetail(Long userId);

    List<AdminProductDTO> getProducts();

    List<AdminOrderDTO> getOrders();

    List<AdminReviewDTO> getReviews();

    List<AdminInquiryDTO> getInquiries();

    Map<String, Object> hideReview(Long reviewId);

    Map<String, Object> deleteReview(Long reviewId);
}