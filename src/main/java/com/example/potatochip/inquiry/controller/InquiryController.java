package com.example.potatochip.inquiry.controller;

import com.example.potatochip.inquiry.dto.InquiryAnswerRequest;
import com.example.potatochip.inquiry.dto.InquiryDTO;
import com.example.potatochip.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @GetMapping("/create")
    public String createInquiryPage() {
        return "inquiry/create";
    }

    @GetMapping("/my")
    public String myInquiriesPage() {
        return "inquiry/my";
    }

    @GetMapping("/admin/inquiries")
    public String adminInquiriesPage() {
        return "admin/inquiries";
    }

    @PostMapping("/api/inquiries")
    @ResponseBody
    public ResponseEntity<?> createInquiry(@RequestBody InquiryDTO inquiryDTO) {
        try {
            return ResponseEntity.ok(inquiryService.createInquiry(inquiryDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/inquiries/my")
    @ResponseBody
    public ResponseEntity<?> getMyInquiries(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(inquiryService.getMyInquiries(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/inquiries/{inquiryId}")
    @ResponseBody
    public ResponseEntity<?> getMyInquiryDetail(
            @PathVariable Long inquiryId,
            @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(inquiryService.getMyInquiryDetail(inquiryId, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/inquiries/{inquiryId}")
    @ResponseBody
    public ResponseEntity<?> updateInquiry(
            @PathVariable Long inquiryId,
            @RequestBody InquiryDTO inquiryDTO
    ) {
        try {
            return ResponseEntity.ok(inquiryService.updateInquiry(inquiryId, inquiryDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/inquiries/{inquiryId}")
    @ResponseBody
    public ResponseEntity<?> deleteInquiry(
            @PathVariable Long inquiryId,
            @RequestParam Long userId
    ) {
        try {
            inquiryService.deleteInquiry(inquiryId, userId);
            return ResponseEntity.ok(Map.of("message", "문의가 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/admin/inquiries")
    @ResponseBody
    public ResponseEntity<?> getAdminInquiries(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(inquiryService.getAdminInquiries(status));
    }

    @GetMapping("/api/admin/inquiries/{inquiryId}")
    @ResponseBody
    public ResponseEntity<?> getAdminInquiryDetail(@PathVariable Long inquiryId) {
        try {
            return ResponseEntity.ok(inquiryService.getAdminInquiryDetail(inquiryId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/inquiries/{inquiryId}/answer")
    @ResponseBody
    public ResponseEntity<?> answerInquiry(
            @PathVariable Long inquiryId,
            @RequestBody InquiryAnswerRequest request
    ) {
        try {
            return ResponseEntity.ok(inquiryService.answerInquiry(inquiryId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}