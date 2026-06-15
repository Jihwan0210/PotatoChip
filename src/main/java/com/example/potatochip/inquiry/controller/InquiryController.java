package com.example.potatochip.inquiry.controller;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.inquiry.dto.InquiryAnswerRequest;
import com.example.potatochip.inquiry.dto.InquiryDTO;
import com.example.potatochip.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final UserRepository userRepository;

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
    public ResponseEntity<?> createInquiry(
            @RequestBody InquiryDTO inquiryDTO,
            Authentication authentication
    ) {
        try {
            Long loginUserId = getLoginUserId(authentication);
            inquiryDTO.setUserId(loginUserId);

            return ResponseEntity.ok(inquiryService.createInquiry(inquiryDTO));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/inquiries/my")
    @ResponseBody
    public ResponseEntity<?> getMyInquiries(Authentication authentication) {
        try {
            Long loginUserId = getLoginUserId(authentication);

            return ResponseEntity.ok(inquiryService.getMyInquiries(loginUserId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/inquiries/{inquiryId}")
    @ResponseBody
    public ResponseEntity<?> getMyInquiryDetail(
            @PathVariable Long inquiryId,
            Authentication authentication
    ) {
        try {
            Long loginUserId = getLoginUserId(authentication);

            return ResponseEntity.ok(inquiryService.getMyInquiryDetail(inquiryId, loginUserId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/inquiries/{inquiryId}")
    @ResponseBody
    public ResponseEntity<?> updateInquiry(
            @PathVariable Long inquiryId,
            @RequestBody InquiryDTO inquiryDTO,
            Authentication authentication
    ) {
        try {
            Long loginUserId = getLoginUserId(authentication);
            inquiryDTO.setUserId(loginUserId);

            return ResponseEntity.ok(inquiryService.updateInquiry(inquiryId, inquiryDTO));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/inquiries/{inquiryId}")
    @ResponseBody
    public ResponseEntity<?> deleteInquiry(
            @PathVariable Long inquiryId,
            Authentication authentication
    ) {
        try {
            Long loginUserId = getLoginUserId(authentication);

            inquiryService.deleteInquiry(inquiryId, loginUserId);
            return ResponseEntity.ok(Map.of("message", "문의가 삭제되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
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
            @RequestBody InquiryAnswerRequest request,
            Authentication authentication
    ) {
        try {
            Long loginUserId = getLoginUserId(authentication);
            request.setAdminId(loginUserId);

            return ResponseEntity.ok(inquiryService.answerInquiry(inquiryId, request));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Long getLoginUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        String email = String.valueOf(authentication.getPrincipal());

        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("로그인 사용자를 찾을 수 없습니다."));

        return user.getId();
    }
}