package com.example.potatochip.inquiry.controller;

import com.example.potatochip.inquiry.dto.InquiryAnswerRequest;
import com.example.potatochip.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/inquiries")
public class AdminInquiryController {

    private final InquiryService inquiryService;

    @GetMapping
    public ResponseEntity<?> getAdminInquiries(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(inquiryService.getAdminInquiries(status));
    }

    @GetMapping("/{inquiryId}")
    public ResponseEntity<?> getAdminInquiryDetail(@PathVariable Long inquiryId) {
        try {
            return ResponseEntity.ok(inquiryService.getAdminInquiryDetail(inquiryId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{inquiryId}/answer")
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