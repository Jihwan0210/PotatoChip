package com.example.potatochip.inquiry.controller;

import com.example.potatochip.inquiry.dto.InquiryDTO;
import com.example.potatochip.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<?> createInquiry(@RequestBody InquiryDTO inquiryDTO) {
        try {
            return ResponseEntity.ok(inquiryService.createInquiry(inquiryDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyInquiries(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(inquiryService.getMyInquiries(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{inquiryId}")
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

    @PutMapping("/{inquiryId}")
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

    @DeleteMapping("/{inquiryId}")
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
}