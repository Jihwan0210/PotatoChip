package com.example.potatochip.board.controller;

import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.board.dto.BoardReportRequest;
import com.example.potatochip.board.service.BoardReportService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
public class BoardReportController {

    private final BoardReportService boardReportService;
    private final JwtUtil jwtUtil;

    @PostMapping("/api/board/{boardId}/reports")
    public ResponseEntity<?> reportBoard(
            @PathVariable Long boardId,
            @RequestBody BoardReportRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, httpRequest);
        return ResponseEntity.ok(boardReportService.reportBoard(boardId, request, resolvedAuthentication));
    }

    @GetMapping("/api/board/{boardId}/reports/status")
    public ResponseEntity<?> getMyReportStatus(
            @PathVariable Long boardId,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, httpRequest);
        return ResponseEntity.ok(boardReportService.getMyReportStatus(boardId, resolvedAuthentication));
    }

    @GetMapping("/api/admin/dashboard/board-reports")
    public ResponseEntity<?> getAdminReportBoards(Authentication authentication, HttpServletRequest httpRequest) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, httpRequest);
        return ResponseEntity.ok(boardReportService.getAdminReportBoards(resolvedAuthentication));
    }

    @GetMapping("/api/admin/dashboard/board-reports/{boardId}")
    public ResponseEntity<?> getAdminBoardReportDetail(
            @PathVariable Long boardId,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, httpRequest);
        return ResponseEntity.ok(boardReportService.getAdminBoardReportDetail(boardId, resolvedAuthentication));
    }

    @PatchMapping("/api/admin/dashboard/board-reports/{boardId}/hide")
    public ResponseEntity<?> hideReportedBoard(
            @PathVariable Long boardId,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, httpRequest);
        return ResponseEntity.ok(boardReportService.hideBoard(boardId, resolvedAuthentication));
    }

    @PatchMapping("/api/admin/dashboard/board-reports/{boardId}/restore")
    public ResponseEntity<?> restoreReportedBoard(
            @PathVariable Long boardId,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, httpRequest);
        return ResponseEntity.ok(boardReportService.restoreBoard(boardId, resolvedAuthentication));
    }

    @DeleteMapping("/api/admin/dashboard/board-reports/{boardId}")
    public ResponseEntity<?> deleteReportedBoard(
            @PathVariable Long boardId,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, httpRequest);
        return ResponseEntity.ok(boardReportService.deleteBoard(boardId, resolvedAuthentication));
    }

    private Authentication resolveAuthentication(Authentication authentication, HttpServletRequest request) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication;
        }

        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return authentication;
        }

        String email = jwtUtil.getEmail(token);
        String role = jwtUtil.getRole(token);

        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (!token.isBlank() && !"null".equalsIgnoreCase(token)) return token;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) return cookie.getValue();
            }
        }

        return null;
    }
}
